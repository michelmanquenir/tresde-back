package com.tresde_creation.tienda_tresde.back.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.ProductResponse;
import com.tresde_creation.tienda_tresde.back.service.ProductService;

/** API pública de la tienda: solo devuelve productos visibles. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<ProductResponse> list(@RequestParam(required = false) String category,
			@RequestParam(required = false, name = "q") String query) {
		return productService.listVisible(category, query);
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable String id) {
		return productService.getVisible(id);
	}
}
