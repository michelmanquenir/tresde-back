package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nueva entrega de inventario: a quién, cuándo y qué productos (con cantidad). */
public record DeliveryRequest(
		@NotBlank(message = "Elige a quién le entregas.")
		String recipientUsername,

		/** Día de la entrega (por defecto, ahora). */
		Instant deliveredAt,

		@Size(max = 500, message = "Máximo 500 caracteres.")
		String note,

		@NotEmpty(message = "Agrega al menos un producto.")
		@Size(max = 50, message = "Máximo 50 productos por entrega.")
		List<@Valid Item> items) {

	public record Item(
			@NotBlank(message = "Elige el producto.")
			String productId,

			@NotNull(message = "Indica la cantidad.")
			@Min(value = 1, message = "La cantidad mínima es 1.")
			@Max(value = 10000, message = "Cantidad demasiado grande.")
			Integer quantity) {}
}
