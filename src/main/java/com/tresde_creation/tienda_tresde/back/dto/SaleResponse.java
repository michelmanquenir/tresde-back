package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Venta tal como la ve el panel. productId puede apuntar a un producto que ya no existe. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleResponse(
		Long id,
		String productId,
		String productName,
		String category,
		String color,
		int quantity,
		int unitPrice,
		Double unitCost, // materia prima por unidad (en ventas antiguas incluía la luz)
		Double unitFilamentCost, // desglose (null en ventas antiguas)
		Double unitAccessoriesCost,
		double reservePercent,
		boolean stockDiscounted,
		String channel,
		String customer,
		String note,
		Long closingId, // cuadratura en que se cerró (no viene si está pendiente)
		Instant soldAt,
		Instant createdAt,
		String soldBy, // usuario que la registró (no viene en ventas antiguas)
		String soldByName) {}
