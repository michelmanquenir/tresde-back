package com.tresde_creation.tienda_tresde.back.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;

/**
 * Convierte un token válido en la sesión del usuario, leyendo el rol desde la base de datos.
 * Así, si el administrador elimina a un trabajador, le cambia el rol o la contraseña, el cambio
 * aplica de inmediato aunque el token siga vigente.
 */
@Component
public class UserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UserRepository users;

	public UserAuthenticationConverter(UserRepository users) {
		this.users = users;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		User user = users.findByUsernameIgnoreCase(jwt.getSubject())
				.orElseThrow(() -> new InvalidBearerTokenException("El usuario ya no existe."));
		Instant since = user.getSessionsSince();
		Instant issuedAt = jwt.getIssuedAt();
		if (since != null && (issuedAt == null || issuedAt.isBefore(since.truncatedTo(ChronoUnit.SECONDS)))) {
			throw new InvalidBearerTokenException("La contraseña cambió: inicia sesión de nuevo.");
		}
		return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
				jwt.getSubject());
	}
}
