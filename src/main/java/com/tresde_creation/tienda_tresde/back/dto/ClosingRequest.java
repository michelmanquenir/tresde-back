package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Cerrar la semana: se incluyen las ventas pendientes vendidas antes de "to".
 * Los montos son lo que realmente se movió; si no vienen, se usan los calculados.
 */
public record ClosingRequest(
		@NotNull(message = "Indica la fecha de corte.") Instant to,
		@PositiveOrZero(message = "No puede ser negativo.") Double materialsReinvested,
		@PositiveOrZero(message = "No puede ser negativo.") Double machineWithdrawn,
		@PositiveOrZero(message = "No puede ser negativo.") Double profitWithdrawn,
		@Size(max = 500, message = "Máximo 500 caracteres.") String note) {}
