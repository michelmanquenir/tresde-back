package com.tresde_creation.tienda_tresde.back.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.tresde_creation.tienda_tresde.back.auth.TokenService;

@Configuration
public class SecurityConfig {

	private static final String UNAUTHORIZED_JSON =
			"{\"message\":\"Tu sesión no es válida o expiró. Inicia sesión nuevamente.\",\"errors\":{}}";
	private static final String FORBIDDEN_JSON =
			"{\"message\":\"No tienes permisos para esta acción.\",\"errors\":{}}";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // API sin cookies: el token va en el header Authorization
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/health", "/api/products/**", "/api/categories/**",
								"/api/images/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
						.authenticationEntryPoint((req, res, ex) -> writeJson(res, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_JSON))
						.accessDeniedHandler((req, res, ex) -> writeJson(res, HttpStatus.FORBIDDEN, FORBIDDEN_JSON)))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, e) -> writeJson(res, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_JSON))
						.accessDeniedHandler((req, res, e) -> writeJson(res, HttpStatus.FORBIDDEN, FORBIDDEN_JSON)));
		return http.build();
	}

	private static void writeJson(jakarta.servlet.http.HttpServletResponse res, HttpStatus status, String body)
			throws java.io.IOException {
		res.setStatus(status.value());
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());
		res.getWriter().write(body);
	}

	/** Lee los roles del claim "roles" del token (ej. ["ADMIN"] -> ROLE_ADMIN). */
	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("roles");
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	SecretKey jwtSecretKey(AppProperties props) {
		byte[] bytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret (JWT_SECRET) debe tener al menos 32 caracteres.");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey key) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey key) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
		return decoder;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AppProperties props) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(props.cors().allowedOrigins().stream().map(String::trim).toList());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}
}
