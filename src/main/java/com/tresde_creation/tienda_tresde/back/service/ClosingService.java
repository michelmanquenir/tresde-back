package com.tresde_creation.tienda_tresde.back.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.ClosingRequest;
import com.tresde_creation.tienda_tresde.back.dto.ClosingResponse;
import com.tresde_creation.tienda_tresde.back.dto.ClosingsOverview;
import com.tresde_creation.tienda_tresde.back.model.Closing;
import com.tresde_creation.tienda_tresde.back.model.CostSettings;
import com.tresde_creation.tienda_tresde.back.model.Sale;
import com.tresde_creation.tienda_tresde.back.repository.ClosingRepository;
import com.tresde_creation.tienda_tresde.back.repository.CostSettingsRepository;
import com.tresde_creation.tienda_tresde.back.repository.SaleRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/**
 * Cuadratura semanal. Cada venta se reparte en:
 *   materia prima = cantidad × costo unitario (filamento + accesorios)  → se reinvierte
 *   máquina       = ingreso × % máquina                                 → se retira y abona a la impresora
 *   ganancia      = ingreso − materia prima − máquina                   → se retira
 * Las ventas quedan marcadas con su cuadratura para no contarlas dos veces.
 */
@Service
public class ClosingService {

	private final ClosingRepository closings;
	private final SaleRepository sales;
	private final CostSettingsRepository settings;

	public ClosingService(ClosingRepository closings, SaleRepository sales, CostSettingsRepository settings) {
		this.closings = closings;
		this.sales = sales;
		this.settings = settings;
	}

	@Transactional(readOnly = true)
	public ClosingsOverview overview() {
		int price = settings.findById(CostSettings.SINGLETON_ID).map(CostSettings::getMachinePrice).orElse(0);
		double recovered = closings.totalMachineWithdrawn();
		return new ClosingsOverview(price, recovered, Math.max(0, price - recovered), sales.countByClosingIdIsNull(),
				closings.findAllByOrderByPeriodToDescIdDesc().stream().map(ClosingService::toResponse).toList());
	}

	/** Lo que se cerraría con este corte (sin guardar nada). */
	@Transactional(readOnly = true)
	public ClosingResponse preview(Instant to) {
		Totals t = totals(sales.findByClosingIdIsNullAndSoldAtLessThanOrderBySoldAtAscIdAsc(to));
		return new ClosingResponse(null, t.from, to, t.count, t.units, t.revenue, t.materials, t.machine, t.profit,
				t.withoutCost, null, null, null, null, null, null);
	}

	@Transactional
	public ClosingResponse create(ClosingRequest req, String username) {
		if (req.to().isAfter(Instant.now().plusSeconds(8 * 24 * 3600))) {
			throw ApiException.badRequest("La fecha de corte no puede ser más allá de esta semana.");
		}
		List<Sale> pending = sales.findByClosingIdIsNullAndSoldAtLessThanOrderBySoldAtAscIdAsc(req.to());
		if (pending.isEmpty()) {
			throw new ApiException(HttpStatus.CONFLICT, "No hay ventas pendientes hasta esa fecha.");
		}
		Totals t = totals(pending);
		Closing c = new Closing();
		c.setPeriodFrom(t.from);
		c.setPeriodTo(req.to());
		c.setSalesCount(t.count);
		c.setUnits(t.units);
		c.setRevenue(t.revenue);
		c.setMaterials(t.materials);
		c.setMachine(t.machine);
		c.setProfit(t.profit);
		c.setMaterialsReinvested(req.materialsReinvested() != null ? req.materialsReinvested() : t.materials);
		c.setMachineWithdrawn(req.machineWithdrawn() != null ? req.machineWithdrawn() : t.machine);
		c.setProfitWithdrawn(req.profitWithdrawn() != null ? req.profitWithdrawn() : Math.max(0, t.profit));
		c.setNote(req.note() == null || req.note().isBlank() ? null : req.note().trim());
		c.setCreatedBy(username);
		Closing saved = closings.save(c);
		pending.forEach(s -> s.setClosingId(saved.getId()));
		return toResponse(saved);
	}

	/** Anula una cuadratura: sus ventas vuelven a quedar pendientes y su retiro de máquina deja de contar. */
	@Transactional
	public void delete(Long id) {
		Closing c = closings.findById(id).orElseThrow(() -> ApiException.notFound("Cuadratura no encontrada."));
		sales.findByClosingId(id).forEach(s -> s.setClosingId(null));
		closings.delete(c);
	}

	private record Totals(Instant from, int count, int units, double revenue, double materials, double machine,
			double profit, int withoutCost) {}

	private static Totals totals(List<Sale> list) {
		int units = 0, withoutCost = 0;
		double revenue = 0, materials = 0, machine = 0;
		Instant from = null;
		for (Sale s : list) {
			double income = (double) s.getQuantity() * s.getUnitPrice();
			units += s.getQuantity();
			revenue += income;
			machine += income * s.getReservePercent() / 100.0;
			if (s.getUnitCost() == null) withoutCost++;
			else materials += s.getUnitCost() * s.getQuantity();
			if (from == null || s.getSoldAt().isBefore(from)) from = s.getSoldAt();
		}
		return new Totals(from, list.size(), units, revenue, materials, machine, revenue - materials - machine, withoutCost);
	}

	private static ClosingResponse toResponse(Closing c) {
		return new ClosingResponse(c.getId(), c.getPeriodFrom(), c.getPeriodTo(), c.getSalesCount(), c.getUnits(),
				c.getRevenue(), c.getMaterials(), c.getMachine(), c.getProfit(), 0,
				c.getMaterialsReinvested(), c.getMachineWithdrawn(), c.getProfitWithdrawn(), c.getNote(),
				c.getCreatedBy(), c.getCreatedAt());
	}
}
