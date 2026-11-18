package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Alta o edición de una categoría desde el panel. */
public record CategoryRequest(
		@NotBlank(message = "Escribe el nombre.") @Size(max = 80, message = "Máximo 80 caracteres.") String name,
		@Size(max = 255, message = "Máximo 255 caracteres.") String description,
		@NotBlank(message = "Elige un icono.") @Pattern(regexp = "^[a-z]{1,30}$", message = "Icono no válido.") String icon,
		@NotNull(message = "Elige un color.") @Min(value = 0, message = "Color no válido.")
		@Max(value = 359, message = "Color no válido.") Integer hue) {}
