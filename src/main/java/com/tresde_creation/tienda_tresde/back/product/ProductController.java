package com.tresde_creation.tienda_tresde.back.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API pública de la tienda: solo devuelve productos visibles. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@GetMapping
	List<ProductResponse> list(@RequestParam(required = false) String category,
			@RequestParam(required = false, name = "q") String query) {
		return service.listVisible(category, query);
	}

	@GetMapping("/{id}")
	ProductResponse get(@PathVariable String id) {
		return service.getVisible(id);
	}
}
