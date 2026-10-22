package com.tresde_creation.tienda_tresde.back.auth;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.common.ApiException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	public record LoginRequest(@NotBlank(message = "Ingresa tu usuario.") String username,
			@NotBlank(message = "Ingresa tu contraseña.") String password) {}

	public record UserInfo(String username, String name) {}

	public record LoginResponse(String token, Instant expiresAt, UserInfo user) {}

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final TokenService tokens;

	public AuthController(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
		this.users = users;
		this.encoder = encoder;
		this.tokens = tokens;
	}

	@PostMapping("/login")
	LoginResponse login(@Valid @RequestBody LoginRequest request) {
		User user = users.findByUsernameIgnoreCase(request.username().trim())
				.filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos."));
		TokenService.IssuedToken token = tokens.issue(user);
		return new LoginResponse(token.value(), token.expiresAt(), new UserInfo(user.getUsername(), user.getName()));
	}

	/** Devuelve el usuario del token actual (sirve para validar la sesión desde el frontend). */
	@GetMapping("/me")
	UserInfo me(@AuthenticationPrincipal Jwt jwt) {
		return new UserInfo(jwt.getSubject(), jwt.getClaimAsString("name"));
	}
}
