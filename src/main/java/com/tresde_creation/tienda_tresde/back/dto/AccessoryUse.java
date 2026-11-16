package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Accesorio que lleva una pieza y cuántos. */
public record AccessoryUse(
		@NotNull(message = "Elige el accesorio.")
		Long accessoryId,

		@NotNull(message = "Indica la cantidad.")
		@Min(value = 1, message = "Al menos 1.")
		@Max(value = 100, message = "Máximo 100 por pieza.")
		Integer quantity) {}
