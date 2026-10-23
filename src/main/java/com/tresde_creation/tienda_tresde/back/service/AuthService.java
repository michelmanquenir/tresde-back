package com.tresde_creation.tienda_tresde.back.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.LoginRequest;
import com.tresde_creation.tienda_tresde.back.dto.LoginResponse;
import com.tresde_creation.tienda_tresde.back.dto.UserInfoResponse;
import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;
import com.tresde_creation.tienda_tresde.back.security.TokenService;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final TokenService tokens;

	public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
		this.users = users;
		this.encoder = encoder;
		this.tokens = tokens;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = users.findByUsernameIgnoreCase(request.username().trim())
				.filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos."));
		TokenService.IssuedToken token = tokens.issue(user);
		return new LoginResponse(token.value(), token.expiresAt(),
				new UserInfoResponse(user.getUsername(), user.getName()));
	}

	/** Datos del usuario a partir de un token ya validado. */
	public UserInfoResponse currentUser(Jwt jwt) {
		return new UserInfoResponse(jwt.getSubject(), jwt.getClaimAsString("name"));
	}
}
