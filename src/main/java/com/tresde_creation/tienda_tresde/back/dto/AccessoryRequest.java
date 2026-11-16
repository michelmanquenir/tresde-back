package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AccessoryRequest(
		@NotBlank(message = "Escribe un nombre.")
		@Size(max = 80, message = "Máximo 80 caracteres.")
		String name,

		@NotNull(message = "Indica el costo.")
		@PositiveOrZero(message = "El costo no puede ser negativo.")
		@Max(value = 1000000, message = "Costo demasiado alto.")
		Integer cost) {}
