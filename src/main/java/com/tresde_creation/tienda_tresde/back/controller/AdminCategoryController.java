package com.tresde_creation.tienda_tresde.back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.CategoryAdminResponse;
import com.tresde_creation.tienda_tresde.back.dto.CategoryRequest;
import com.tresde_creation.tienda_tresde.back.service.CategoryService;

import jakarta.validation.Valid;

/** Mantenedor de categorías del panel. */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

	private final CategoryService service;

	public AdminCategoryController(CategoryService service) {
		this.service = service;
	}

	@GetMapping
	public List<CategoryAdminResponse> list() {
		return service.adminList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryAdminResponse create(@Valid @RequestBody CategoryRequest request) {
		return service.create(request);
	}

	/** Cambia el orden en que aparecen en la tienda. Body: ["figuras", "decoracion", ...] */
	@PostMapping("/reorder")
	public List<CategoryAdminResponse> reorder(@RequestBody List<String> slugs) {
		return service.reorder(slugs);
	}

	@PutMapping("/{slug}")
	public CategoryAdminResponse update(@PathVariable String slug, @Valid @RequestBody CategoryRequest request) {
		return service.update(slug, request);
	}

	@DeleteMapping("/{slug}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String slug) {
		service.delete(slug);
	}
}
