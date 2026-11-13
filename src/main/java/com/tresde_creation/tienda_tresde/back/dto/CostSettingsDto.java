package com.tresde_creation.tienda_tresde.back.dto;

import java.util.Map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.tresde_creation.tienda_tresde.back.model.Material;

/** Configuración de costos que se edita en el panel (Admin → Configuración). */
public record CostSettingsDto(
		@NotNull(message = "Indica el valor hora de la máquina.")
		@PositiveOrZero(message = "El valor hora no puede ser negativo.")
		@DecimalMax(value = "1000000", message = "Valor hora demasiado alto.")
		Double machineHourCost,

		@NotNull(message = "Indica el porcentaje para la máquina.")
		@DecimalMin(value = "0", message = "El porcentaje no puede ser negativo.")
		@DecimalMax(value = "90", message = "Máximo 90%.")
		Double machineReservePercent,

		@NotNull(message = "Indica el multiplicador de venta.")
		@DecimalMin(value = "1", message = "El multiplicador debe ser al menos 1.")
		@DecimalMax(value = "50", message = "Máximo 50.")
		Double priceMultiplier,

		@NotNull(message = "Indica el precio del filamento.")
		Map<Material, @NotNull(message = "Indica el precio.") @PositiveOrZero(message = "El precio no puede ser negativo.") Integer> filamentPricePerKg) {}
