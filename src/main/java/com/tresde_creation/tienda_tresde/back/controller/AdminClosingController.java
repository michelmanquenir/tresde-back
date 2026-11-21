package com.tresde_creation.tienda_tresde.back.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.ClosingRequest;
import com.tresde_creation.tienda_tresde.back.dto.ClosingResponse;
import com.tresde_creation.tienda_tresde.back.dto.ClosingsOverview;
import com.tresde_creation.tienda_tresde.back.service.ClosingService;

import jakarta.validation.Valid;

/** Cuadraturas semanales (retiros de ganancia y máquina). Solo rol ADMIN (ver security/SecurityConfig). */
@RestController
@RequestMapping("/api/admin/closings")
public class AdminClosingController {

	private final ClosingService service;

	public AdminClosingController(ClosingService service) {
		this.service = service;
	}

	@GetMapping
	public ClosingsOverview overview() {
		return service.overview();
	}

	@GetMapping("/preview")
	public ClosingResponse preview(@RequestParam Instant to) {
		return service.preview(to);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClosingResponse create(@Valid @RequestBody ClosingRequest request, @AuthenticationPrincipal Jwt jwt) {
		return service.create(request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
