package com.tresde_creation.tienda_tresde.back.dto;

import java.util.List;

/** Pantalla de cuadratura: avance del pago de la máquina, ventas pendientes y cuadraturas anteriores. */
public record ClosingsOverview(
		int machinePrice,
		double machineRecovered, // suma de lo retirado para la máquina
		double machineRemaining, // lo que falta (nunca negativo)
		long pendingSales, // ventas aún sin cuadratura (de cualquier fecha)
		List<ClosingResponse> closings) {}
