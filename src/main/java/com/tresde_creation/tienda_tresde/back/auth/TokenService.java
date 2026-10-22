package com.tresde_creation.tienda_tresde.back.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.tresde_creation.tienda_tresde.back.config.AppProperties;

/** Emite tokens JWT firmados (HS256) para los usuarios que inician sesión. */
@Service
public class TokenService {

	public static final String ISSUER = "tresde-creation";

	public record IssuedToken(String value, Instant expiresAt) {}

	private final JwtEncoder encoder;
	private final Duration validity;

	public TokenService(JwtEncoder encoder, AppProperties props) {
		this.encoder = encoder;
		this.validity = Duration.ofHours(props.jwt().expirationHours());
	}

	public IssuedToken issue(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(validity);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject(user.getUsername())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("name", user.getName())
				.claim("roles", List.of(user.getRole()))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(token, expiresAt);
	}
}
