package com.tresde_creation.tienda_tresde.back.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.tresde_creation.tienda_tresde.back.model.Material;

/** Datos que envía el panel para crear o editar un producto. */
public record ProductRequest(
		@NotBlank(message = "Escribe un nombre.")
		@Size(max = 120, message = "Máximo 120 caracteres.")
		String name,

		@NotBlank(message = "Elige una categoría.")
		String category,

		@NotNull(message = "Indica el precio.")
		@Positive(message = "El precio debe ser mayor a 0.")
		Integer price,

		@Positive(message = "El precio anterior debe ser mayor a 0.")
		Integer oldPrice,

		@NotBlank(message = "Agrega una descripción.")
		@Size(max = 2000, message = "Máximo 2000 caracteres.")
		String description,

		@NotNull(message = "Elige un material.")
		Material material,

		@Positive(message = "La altura debe ser mayor a 0.")
		@DecimalMax(value = "500", message = "La altura máxima es 500 cm.")
		Double heightCm,

		@PositiveOrZero(message = "Las horas no pueden ser negativas.")
		Double printHours,

		@NotEmpty(message = "Agrega al menos un color.")
		@Size(max = 16, message = "Máximo 16 colores.")
		List<@Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color inválido (usa #RRGGBB).") String> colors,

		@Size(max = 6, message = "Máximo 6 fotos.")
		List<@NotBlank String> images,

		@JsonProperty("isNew") Boolean isNew,
		Boolean bestSeller,
		Boolean hidden,

		@Valid ProductCost cost) {}
