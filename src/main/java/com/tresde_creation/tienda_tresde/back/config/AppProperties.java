package com.tresde_creation.tienda_tresde.back.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración propia de la tienda (prefijo "app" en application.properties). */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, Jwt jwt, Admin admin) {

	public record Cors(List<String> allowedOrigins) {}

	public record Jwt(String secret, int expirationHours) {}

	public record Admin(String username, String password, String name) {}
}
