package com.tresde_creation.tienda_tresde.back.controller;

import java.util.List;

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

import com.tresde_creation.tienda_tresde.back.dto.ImportProductRequest;
import com.tresde_creation.tienda_tresde.back.dto.ProductRequest;
import com.tresde_creation.tienda_tresde.back.dto.ProductResponse;
import com.tresde_creation.tienda_tresde.back.dto.StockRequest;
import com.tresde_creation.tienda_tresde.back.dto.VisibilityRequest;
import com.tresde_creation.tienda_tresde.back.service.ProductService;

import jakarta.validation.Valid;

/** API del panel de administración. Requiere token con rol ADMIN (ver security/SecurityConfig). */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

	private final ProductService productService;

	public AdminProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<ProductResponse> list() {
		return productService.listAll();
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable String id) {
		return productService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody ProductRequest request) {
		return productService.create(request);
	}

	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@PatchMapping("/{id}/visibility")
	public ProductResponse setVisibility(@PathVariable String id, @Valid @RequestBody VisibilityRequest request) {
		return productService.setHidden(id, request.hidden());
	}

	@PatchMapping("/{id}/stock")
	public ProductResponse setStock(@PathVariable String id, @Valid @RequestBody StockRequest request) {
		return productService.setStock(id, request.stock());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		productService.delete(id);
	}

	/** Reemplaza todo el catálogo por el contenido de un respaldo JSON. */
	@PostMapping("/import")
	public List<ProductResponse> importAll(@RequestBody List<ImportProductRequest> items) {
		return productService.replaceAll(items);
	}
}
