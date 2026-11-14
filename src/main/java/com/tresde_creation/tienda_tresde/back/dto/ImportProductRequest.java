package com.tresde_creation.tienda_tresde.back.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tresde_creation.tienda_tresde.back.model.Material;

/** Cada producto del JSON que exporta el panel (incluye id y rating, que no se editan en el formulario). */
public record ImportProductRequest(
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
		@JsonProperty("isNew") Boolean isNew,
		Boolean bestSeller,
		Boolean hidden,
		Double rating,
		Double printHours,
		Integer stock,
		ProductCost cost) {

	public ProductRequest toProductRequest() {
		return new ProductRequest(name, category, price, oldPrice, description, material, heightCm, printHours,
				colors, images, isNew, bestSeller, hidden, stock == null ? 0 : stock, cost);
	}
}
