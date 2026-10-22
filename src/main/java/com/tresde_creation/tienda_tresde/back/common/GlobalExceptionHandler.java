package com.tresde_creation.tienda_tresde.back.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Convierte las excepciones en respuestas JSON con el formato de {@link ErrorResponse}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErrorResponse> handleApi(ApiException e) {
		return ResponseEntity.status(e.getStatus()).body(ErrorResponse.of(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fe : e.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
		}
		return ResponseEntity.badRequest().body(new ErrorResponse("Revisa los datos enviados.", errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().body(ErrorResponse.of("El cuerpo de la petición no es válido."));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
				.body(ErrorResponse.of("La imagen es demasiado grande (máximo 5 MB)."));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("Recurso no encontrado."));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ErrorResponse.of("Método no permitido."));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("Error inesperado", e);
		return ResponseEntity.internalServerError().body(ErrorResponse.of("Ocurrió un error inesperado en el servidor."));
	}
}
