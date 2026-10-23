package com.tresde_creation.tienda_tresde.back.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.LoginRequest;
import com.tresde_creation.tienda_tresde.back.dto.LoginResponse;
import com.tresde_creation.tienda_tresde.back.dto.UserInfoResponse;
import com.tresde_creation.tienda_tresde.back.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	/** Devuelve el usuario del token actual (sirve para validar la sesión desde el frontend). */
	@GetMapping("/me")
	public UserInfoResponse me(@AuthenticationPrincipal Jwt jwt) {
		return authService.currentUser(jwt);
	}
}
