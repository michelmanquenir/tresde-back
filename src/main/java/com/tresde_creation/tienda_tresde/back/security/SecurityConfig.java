package com.tresde_creation.tienda_tresde.back.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

/** Reglas de acceso de la API: qué rutas son públicas y cuáles requieren el panel (ADMIN o STAFF). */
@Configuration
public class SecurityConfig {

	private static final String UNAUTHORIZED_JSON =
			"{\"message\":\"Tu sesión no es válida o expiró. Inicia sesión nuevamente.\",\"errors\":{}}";
	private static final String FORBIDDEN_JSON =
			"{\"message\":\"No tienes permisos para esta acción.\",\"errors\":{}}";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, UserAuthenticationConverter userConverter) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // API sin cookies: el token va en el header Authorization
				.cors(Customizer.withDefaults()) // ver config/CorsConfig
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						// Monitores externos (ej. UptimeRobot) suelen usar HEAD para mantener el servidor despierto
						.requestMatchers(HttpMethod.HEAD, "/api/health").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/health", "/api/products/**", "/api/categories/**",
								"/api/images/**").permitAll()
						// Trabajadores (STAFF): todo el panel menos usuarios, cuadraturas y cambiar la configuración de costos
						.requestMatchers("/api/admin/users/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/closings/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/admin/settings/**").hasRole("ADMIN")
						// Vendedores (SELLER): solo registrar ventas y ver las suyas (anular, no)
						.requestMatchers(HttpMethod.GET, "/api/admin/sales").hasAnyRole("ADMIN", "STAFF", "SELLER")
						.requestMatchers(HttpMethod.POST, "/api/admin/sales").hasAnyRole("ADMIN", "STAFF", "SELLER")
						.requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "STAFF")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(userConverter))
						.authenticationEntryPoint((req, res, ex) -> writeJson(res, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_JSON))
						.accessDeniedHandler((req, res, ex) -> writeJson(res, HttpStatus.FORBIDDEN, FORBIDDEN_JSON)))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, e) -> writeJson(res, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_JSON))
						.accessDeniedHandler((req, res, e) -> writeJson(res, HttpStatus.FORBIDDEN, FORBIDDEN_JSON)));
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private static void writeJson(HttpServletResponse res, HttpStatus status, String body) throws IOException {
		res.setStatus(status.value());
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());
		res.getWriter().write(body);
	}
}
