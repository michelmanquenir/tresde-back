package com.tresde_creation.tienda_tresde.back.utils;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Utilidades para las URLs de las fotos guardadas en el servidor (/api/images/{id}). */
public final class ImageUrls {

	public static final String PATH_PREFIX = "/api/images/";

	private static final Pattern STORED_PATH = Pattern.compile("/api/images/([0-9a-fA-F-]{36})");

	private ImageUrls() {}

	/** Ruta relativa con la que se guarda la foto dentro del producto. */
	public static String relative(UUID id) {
		return PATH_PREFIX + id;
	}

	/** Si la URL apunta a una foto guardada aquí, devuelve su id. */
	public static Optional<UUID> storedId(String url) {
		Matcher m = STORED_PATH.matcher(url);
		if (!m.find()) return Optional.empty();
		try {
			return Optional.of(UUID.fromString(m.group(1)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	/** Convierte /api/images/{id} en URL completa (http://host/api/images/{id}) según la petición actual. */
	public static String absolute(String url) {
		if (!url.startsWith(PATH_PREFIX)) return url;
		try {
			return ServletUriComponentsBuilder.fromCurrentContextPath().path(url).toUriString();
		} catch (IllegalStateException e) {
			return url; // fuera de una petición HTTP
		}
	}
}
