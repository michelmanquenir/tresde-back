package com.tresde_creation.tienda_tresde.back.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.SaleRequest;
import com.tresde_creation.tienda_tresde.back.dto.SaleResponse;
import com.tresde_creation.tienda_tresde.back.service.SaleService;

import jakarta.validation.Valid;

/** Ventas del panel (requiere rol ADMIN, ver SecurityConfig: /api/admin/**). */
@RestController
@RequestMapping("/api/admin/sales")
public class AdminSalesController {

	private final SaleService saleService;

	public AdminSalesController(SaleService saleService) {
		this.saleService = saleService;
	}

	/** ?from=...&to=... en ISO-8601 (opcionales). */
	@GetMapping
	public List<SaleResponse> list(@RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to) {
		return saleService.list(from, to);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SaleResponse create(@Valid @RequestBody SaleRequest request) {
		return saleService.create(request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		saleService.delete(id);
	}
}
