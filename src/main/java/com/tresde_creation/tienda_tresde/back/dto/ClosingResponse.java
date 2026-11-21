package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Cuadratura (o vista previa, sin id): montos calculados y lo que realmente se retiró/reinvirtió. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClosingResponse(
		Long id,
		Instant periodFrom,
		Instant periodTo,
		int salesCount,
		int units,
		double revenue,
		double materials, // materia prima: se reinvierte
		double machine, // % para la máquina: se retira
		double profit, // ganancia: se retira
		int salesWithoutCost, // ventas sin costos cargados (su materia prima cuenta como 0)
		Double materialsReinvested,
		Double machineWithdrawn,
		Double profitWithdrawn,
		String note,
		String createdBy,
		Instant createdAt) {}
