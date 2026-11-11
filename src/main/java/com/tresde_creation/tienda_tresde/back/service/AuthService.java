package com.tresde_creation.tienda_tresde.back.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.LoginRequest;
import com.tresde_creation.tienda_tresde.back.dto.LoginResponse;
import com.tresde_creation.tienda_tresde.back.dto.RegisterRequest;
import com.tresde_creation.tienda_tresde.back.dto.UserInfoResponse;
import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;
import com.tresde_creation.tienda_tresde.back.security.TokenService;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

@Service
public class AuthService {

	public static final String ROLE_CUSTOMER = "CUSTOMER";

	private static final String EMAIL_TAKEN = "Ya existe una cuenta con ese correo.";

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
		return sessionFor(user);
	}

	/** Crea una cuenta de cliente (el correo es su usuario) y deja la sesión iniciada. */
	@Transactional
	public LoginResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (users.existsByUsernameIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, EMAIL_TAKEN);
		User user;
		try {
			user = users.saveAndFlush(
					new User(email, encoder.encode(request.password()), request.name().trim(), ROLE_CUSTOMER));
		} catch (DataIntegrityViolationException e) {
			// Dos registros simultáneos con el mismo correo
			throw new ApiException(HttpStatus.CONFLICT, EMAIL_TAKEN);
		}
		return sessionFor(user);
	}

	/** Datos del usuario a partir de un token ya validado. */
	public UserInfoResponse currentUser(Jwt jwt) {
		List<String> roles = jwt.getClaimAsStringList("roles");
		String role = roles == null || roles.isEmpty() ? null : roles.get(0);
		return new UserInfoResponse(jwt.getSubject(), jwt.getClaimAsString("name"), role);
	}

	private LoginResponse sessionFor(User user) {
		TokenService.IssuedToken token = tokens.issue(user);
		return new LoginResponse(token.value(), token.expiresAt(),
				new UserInfoResponse(user.getUsername(), user.getName(), user.getRole()));
	}
}
