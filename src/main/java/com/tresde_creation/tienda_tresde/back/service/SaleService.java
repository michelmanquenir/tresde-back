package com.tresde_creation.tienda_tresde.back.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.SaleRequest;
import com.tresde_creation.tienda_tresde.back.dto.SaleResponse;
import com.tresde_creation.tienda_tresde.back.model.CostSettings;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.model.Sale;
import com.tresde_creation.tienda_tresde.back.repository.CostSettingsRepository;
import com.tresde_creation.tienda_tresde.back.repository.ProductRepository;
import com.tresde_creation.tienda_tresde.back.repository.SaleRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/** Registro de ventas hechas a mano desde el panel (descuenta y devuelve stock). */
@Service
public class SaleService {

	private static final Instant MIN = Instant.parse("2000-01-01T00:00:00Z");
	private static final Instant MAX = Instant.parse("9999-01-01T00:00:00Z");

	private final SaleRepository sales;
	private final ProductRepository products;
	private final CostSettingsRepository costSettings;

	public SaleService(SaleRepository sales, ProductRepository products, CostSettingsRepository costSettings) {
		this.sales = sales;
		this.products = products;
		this.costSettings = costSettings;
	}

	@Transactional(readOnly = true)
	public List<SaleResponse> list(Instant from, Instant to) {
		return sales.findBySoldAtGreaterThanEqualAndSoldAtLessThanOrderBySoldAtDescIdDesc(
				from == null ? MIN : from, to == null ? MAX : to)
				.stream().map(SaleService::toResponse).toList();
	}

	@Transactional
	public SaleResponse create(SaleRequest req) {
		Product product = products.findById(req.productId())
				.orElseThrow(() -> ApiException.notFound("Producto no encontrado."));
		// Toda venta descuenta del stock: no se puede vender más de lo disponible
		if (req.quantity() > product.getStock()) {
			throw new ApiException(HttpStatus.CONFLICT, product.getStock() == 0
					? "\"" + product.getName() + "\" no tiene stock. Si imprimiste más unidades, súmalas al stock primero."
					: "Solo quedan " + product.getStock() + " unidades de \"" + product.getName() + "\".");
		}
		if (req.soldAt() != null && req.soldAt().isAfter(Instant.now().plusSeconds(24 * 3600))) {
			throw ApiException.badRequest("La fecha de la venta no puede ser futura.");
		}
		CostSettings settings = costSettings.findById(CostSettings.SINGLETON_ID).orElse(null);

		Sale s = new Sale();
		s.setProductId(product.getId());
		s.setProductName(product.getName());
		s.setCategory(product.getCategory());
		s.setColor(blankToNull(req.color()) == null ? null : req.color().toLowerCase());
		s.setQuantity(req.quantity());
		s.setUnitPrice(req.unitPrice() != null ? req.unitPrice() : product.getPrice());
		s.setUnitCost(settings == null ? null : unitCost(product, settings));
		s.setReservePercent(settings == null ? 0 : settings.getMachineReservePercent());
		s.setStockDiscounted(true);
		s.setChannel(blankToNull(req.channel()));
		s.setCustomer(blankToNull(req.customer()));
		s.setNote(blankToNull(req.note()));
		s.setSoldAt(req.soldAt());
		product.setStock(product.getStock() - req.quantity());
		return toResponse(sales.save(s));
	}

	/** Anula una venta. Si había descontado stock y el producto sigue existiendo, lo devuelve. */
	@Transactional
	public void delete(Long id) {
		Sale s = sales.findById(id).orElseThrow(() -> ApiException.notFound("Venta no encontrada."));
		if (s.isStockDiscounted() && s.getProductId() != null) {
			products.findById(s.getProductId()).ifPresent(p -> p.setStock(p.getStock() + s.getQuantity()));
		}
		sales.delete(s);
	}

	/** Mismo cálculo que el panel (utils/pricing.ts). null si el producto no tiene datos de costo. */
	static Double unitCost(Product p, CostSettings s) {
		double grams = p.getGrams() == null ? 0 : p.getGrams();
		double hours = p.getBatchHours() == null ? 0 : p.getBatchHours();
		int accessories = p.getAccessoriesCost() == null ? 0 : p.getAccessoriesCost();
		if (grams == 0 && hours == 0 && accessories == 0) return null;
		int pieces = Math.max(1, p.getBatchPieces() == null ? 1 : p.getBatchPieces());
		double filament = grams * s.getFilamentPrice(p.getMaterial()) / 1000.0;
		double machine = hours * s.getMachineHourCost() / pieces;
		return filament + machine + accessories;
	}

	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v.trim();
	}

	private static SaleResponse toResponse(Sale s) {
		return new SaleResponse(s.getId(), s.getProductId(), s.getProductName(), s.getCategory(), s.getColor(),
				s.getQuantity(), s.getUnitPrice(), s.getUnitCost(), s.getReservePercent(), s.isStockDiscounted(),
				s.getChannel(), s.getCustomer(), s.getNote(), s.getSoldAt(), s.getCreatedAt());
	}
}
