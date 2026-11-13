package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.tresde_creation.tienda_tresde.back.model.Material;

/** Producto tal como lo recibe el frontend (mismo formato que el tipo Product de TypeScript). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
		String id,
		String name,
		String category,
		Integer price,
		Integer oldPrice,
		String description,
		Material material,
		List<String> colors,
		List<String> images,
		Double heightCm,
		@JsonProperty("isNew") boolean isNew,
		boolean bestSeller,
		boolean hidden,
		Double rating,
		Double printHours,
		Instant createdAt,
		Instant updatedAt,
		ProductCost cost) {} // cost solo se envía al panel
