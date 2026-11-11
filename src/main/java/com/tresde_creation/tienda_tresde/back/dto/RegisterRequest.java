package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "Ingresa tu nombre.") @Size(max = 100, message = "El nombre es demasiado largo.") String name,
		@NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo válido.")
		@Size(max = 255, message = "El correo es demasiado largo.") String email,
		@NotBlank(message = "Ingresa una contraseña.")
		@Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.") String password) {}
