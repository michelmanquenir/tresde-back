package com.tresde_creation.tienda_tresde.back.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tresde_creation.tienda_tresde.back.common.ApiException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

/** API del panel de administración. Requiere token con rol ADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

	public record VisibilityRequest(@NotNull Boolean hidden) {}

	/** Formato de cada producto en el JSON exportado por el panel. */
	public record ImportItem(String id, String name, String category, Integer price, Integer oldPrice,
			String description, Material material, List<String> colors, List<String> images, Double heightCm,
			@JsonProperty("isNew") Boolean isNew, Boolean bestSeller, Boolean hidden, Double rating,
			Double printHours) {

		ProductRequest toRequest() {
			return new ProductRequest(name, category, price, oldPrice, description, material, heightCm, printHours,
					colors, images, isNew, bestSeller, hidden);
		}
	}

	private final ProductService service;
	private final Validator validator;

	public AdminProductController(ProductService service, Validator validator) {
		this.service = service;
		this.validator = validator;
	}

	@GetMapping
	List<ProductResponse> list() {
		return service.listAll();
	}

	@GetMapping("/{id}")
	ProductResponse get(@PathVariable String id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse create(@Valid @RequestBody ProductRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	ProductResponse update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
		return service.update(id, request);
	}

	@PatchMapping("/{id}/visibility")
	ProductResponse setVisibility(@PathVariable String id, @Valid @RequestBody VisibilityRequest request) {
		return service.setHidden(id, request.hidden());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id) {
		service.delete(id);
	}

	/** Reemplaza TODO el catálogo por el contenido de un respaldo JSON. */
	@PostMapping("/import")
	List<ProductResponse> importAll(@RequestBody List<ImportItem> items) {
		if (items == null || items.isEmpty()) throw ApiException.badRequest("El archivo no contiene productos.");
		List<ProductService.ImportedProduct> valid = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			ImportItem item = items.get(i);
			ProductRequest req = item.toRequest();
			Set<ConstraintViolation<ProductRequest>> violations = validator.validate(req);
			if (!violations.isEmpty()) {
				ConstraintViolation<ProductRequest> v = violations.iterator().next();
				String label = item.name() != null ? "\"" + item.name() + "\"" : "#" + (i + 1);
				throw ApiException.badRequest("Producto " + label + ": " + v.getPropertyPath() + " - " + v.getMessage());
			}
			valid.add(new ProductService.ImportedProduct(item.id(), item.rating(), req));
		}
		return service.replaceAll(valid);
	}
}
