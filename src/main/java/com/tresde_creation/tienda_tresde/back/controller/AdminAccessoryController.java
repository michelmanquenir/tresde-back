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

import com.tresde_creation.tienda_tresde.back.dto.AccessoryRequest;
import com.tresde_creation.tienda_tresde.back.dto.AccessoryResponse;
import com.tresde_creation.tienda_tresde.back.service.AccessoryService;

import jakarta.validation.Valid;

/** Catálogo de accesorios del panel (requiere rol ADMIN). */
@RestController
@RequestMapping("/api/admin/accessories")
public class AdminAccessoryController {

	private final AccessoryService service;

	public AdminAccessoryController(AccessoryService service) {
		this.service = service;
	}

	@GetMapping
	public List<AccessoryResponse> list() {
		return service.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccessoryResponse create(@Valid @RequestBody AccessoryRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	public AccessoryResponse update(@PathVariable Long id, @Valid @RequestBody AccessoryRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
