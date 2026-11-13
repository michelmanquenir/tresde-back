package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Datos para calcular el costo de un producto. Solo se envían al panel de administración.
 * El cálculo se hace en el panel con la configuración de costos (ver CostSettingsDto).
 */
public record ProductCost(
		@PositiveOrZero(message = "Los gramos no pueden ser negativos.")
		@DecimalMax(value = "100000", message = "Demasiados gramos.")
		Double grams,

		@PositiveOrZero(message = "Las horas no pueden ser negativas.")
		@DecimalMax(value = "1000", message = "Máximo 1000 horas.")
		Double batchHours,

		@Min(value = 1, message = "Al menos 1 pieza por lote.")
		@Max(value = 10000, message = "Máximo 10000 piezas por lote.")
		Integer batchPieces,

		@PositiveOrZero(message = "El costo de accesorios no puede ser negativo.")
		Integer accessoriesCost) {}
