package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Venta que registra el administrador. Si no se indica precio se usa el actual del producto. */
public record SaleRequest(
		@NotBlank(message = "Elige el producto vendido.")
		String productId,

		@Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color inválido.")
		String color,

		@NotNull(message = "Indica la cantidad.")
		@Min(value = 1, message = "La cantidad mínima es 1.")
		@Max(value = 10000, message = "Cantidad demasiado grande.")
		Integer quantity,

		@Positive(message = "El precio debe ser mayor a 0.")
		Integer unitPrice,

		/** true (por defecto): descuenta las unidades del stock. */
		Boolean discountStock,

		@Size(max = 30, message = "Máximo 30 caracteres.")
		String channel,

		@Size(max = 120, message = "Máximo 120 caracteres.")
		String customer,

		@Size(max = 500, message = "Máximo 500 caracteres.")
		String note,

		/** Fecha de la venta (por defecto, ahora). */
		Instant soldAt) {}
