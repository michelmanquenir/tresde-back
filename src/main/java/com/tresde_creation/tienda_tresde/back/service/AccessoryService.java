package com.tresde_creation.tienda_tresde.back.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.AccessoryRequest;
import com.tresde_creation.tienda_tresde.back.dto.AccessoryResponse;
import com.tresde_creation.tienda_tresde.back.model.Accessory;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.model.ProductAccessory;
import com.tresde_creation.tienda_tresde.back.repository.AccessoryRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/** Catálogo de accesorios y cálculo de su costo dentro de cada producto. */
@Service
public class AccessoryService {

	private static final String DUPLICATED = "Ya existe un accesorio con ese nombre.";

	private final AccessoryRepository accessories;

	public AccessoryService(AccessoryRepository accessories) {
		this.accessories = accessories;
	}

	@Transactional(readOnly = true)
	public List<AccessoryResponse> list() {
		Map<Long, Long> usage = usage();
		return accessories.findAllByOrderByNameAsc().stream()
				.map(a -> new AccessoryResponse(a.getId(), a.getName(), a.getCost(), usage.getOrDefault(a.getId(), 0L)))
				.toList();
	}

	@Transactional
	public AccessoryResponse create(AccessoryRequest req) {
		String name = req.name().trim();
		if (accessories.existsByNameIgnoreCase(name)) throw new ApiException(HttpStatus.CONFLICT, DUPLICATED);
		Accessory a = new Accessory();
		a.setName(name);
		a.setCost(req.cost());
		return save(a);
	}

	@Transactional
	public AccessoryResponse update(Long id, AccessoryRequest req) {
		Accessory a = find(id);
		String name = req.name().trim();
		if (accessories.existsByNameIgnoreCaseAndIdNot(name, id)) throw new ApiException(HttpStatus.CONFLICT, DUPLICATED);
		a.setName(name);
		a.setCost(req.cost());
		return save(a);
	}

	/** Lo quita también de los productos que lo usaban (la base de datos borra en cascada). */
	@Transactional
	public void delete(Long id) {
		accessories.delete(find(id));
	}

	/** Costo actual de cada accesorio, por id. */
	@Transactional(readOnly = true)
	public Map<Long, Integer> costs() {
		Map<Long, Integer> map = new HashMap<>();
		accessories.findAll().forEach(a -> map.put(a.getId(), a.getCost()));
		return map;
	}

	/** Suma de (costo × cantidad) de los accesorios del producto; null si no lleva ninguno. */
	public static Integer costOf(Product p, Map<Long, Integer> costs) {
		if (p.getAccessories().isEmpty()) return null;
		int total = 0;
		for (ProductAccessory pa : p.getAccessories()) {
			total += costs.getOrDefault(pa.getAccessoryId(), 0) * pa.getQuantity();
		}
		return total;
	}

	private Map<Long, Long> usage() {
		Map<Long, Long> usage = new HashMap<>();
		for (Object[] row : accessories.countUsage()) {
			usage.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
		}
		return usage;
	}

	private Accessory find(Long id) {
		return accessories.findById(id).orElseThrow(() -> ApiException.notFound("Accesorio no encontrado."));
	}

	private AccessoryResponse save(Accessory a) {
		Accessory saved;
		try {
			saved = accessories.saveAndFlush(a);
		} catch (DataIntegrityViolationException e) {
			throw new ApiException(HttpStatus.CONFLICT, DUPLICATED);
		}
		return new AccessoryResponse(saved.getId(), saved.getName(), saved.getCost(),
				usage().getOrDefault(saved.getId(), 0L));
	}
}
