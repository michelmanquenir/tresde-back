package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequest(
		@NotNull(message = "Indica el stock.")
		@PositiveOrZero(message = "El stock no puede ser negativo.")
		@Max(value = 100000, message = "El stock máximo es 100000.")
		Integer stock) {}
