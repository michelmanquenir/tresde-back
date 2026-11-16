package com.tresde_creation.tienda_tresde.back.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.UserRequest;
import com.tresde_creation.tienda_tresde.back.dto.UserResponse;
import com.tresde_creation.tienda_tresde.back.service.UserService;

import jakarta.validation.Valid;

/** Usuarios del panel (administradores y trabajadores). Solo rol ADMIN (ver security/SecurityConfig). */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

	private final UserService service;

	public AdminUserController(UserService service) {
		this.service = service;
	}

	@GetMapping
	public List<UserResponse> list() {
		return service.list();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse create(@Valid @RequestBody UserRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return service.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		service.delete(id, jwt.getSubject());
	}
}
