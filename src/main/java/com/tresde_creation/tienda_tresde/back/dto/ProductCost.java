package com.tresde_creation.tienda_tresde.back.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

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

		@Size(max = 20, message = "Máximo 20 accesorios distintos.")
		List<@Valid AccessoryUse> accessories,

		/** Solo de salida: suma de (costo actual del accesorio × cantidad). Se ignora al guardar. */
		Integer accessoriesCost) {}
