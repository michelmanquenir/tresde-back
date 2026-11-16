package com.tresde_creation.tienda_tresde.back.utils;

import java.util.Map;

import org.springframework.http.HttpStatus;

/** Error de negocio con su código HTTP y un mensaje apto para mostrar al usuario. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final Map<String, String> fieldErrors;

	public ApiException(HttpStatus status, String message) {
		this(status, message, Map.of());
	}

	/** Con errores por campo (se muestran bajo cada input del formulario). */
	public ApiException(HttpStatus status, String message, Map<String, String> fieldErrors) {
		super(message);
		this.status = status;
		this.fieldErrors = fieldErrors;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	public static ApiException notFound(String message) {
		return new ApiException(HttpStatus.NOT_FOUND, message);
	}

	public static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
