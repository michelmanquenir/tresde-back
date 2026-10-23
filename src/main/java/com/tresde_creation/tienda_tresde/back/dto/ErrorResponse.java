package com.tresde_creation.tienda_tresde.back.dto;

import java.util.Map;

/**
 * Formato único de error que recibe el frontend.
 * Ej: { "message": "Revisa los campos", "errors": { "price": "debe ser mayor que 0" } }
 */
public record ErrorResponse(String message, Map<String, String> errors) {

	public static ErrorResponse of(String message) {
		return new ErrorResponse(message, Map.of());
	}
}
