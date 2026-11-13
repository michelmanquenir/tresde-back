package com.tresde_creation.tienda_tresde.back.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.CostSettingsDto;
import com.tresde_creation.tienda_tresde.back.service.CostSettingsService;

import jakarta.validation.Valid;

/** Configuración de costos de impresión (solo administrador, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/settings/costs")
public class AdminSettingsController {

	private final CostSettingsService service;

	public AdminSettingsController(CostSettingsService service) {
		this.service = service;
	}

	@GetMapping
	public CostSettingsDto get() {
		return service.get();
	}

	@PutMapping
	public CostSettingsDto update(@Valid @RequestBody CostSettingsDto dto) {
		return service.update(dto);
	}
}
