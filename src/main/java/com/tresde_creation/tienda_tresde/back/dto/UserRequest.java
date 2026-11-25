package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta o edición de un usuario del panel.
 * username: solo se usa al crear (no se puede cambiar después).
 * password: obligatoria al crear; al editar, vacía = no se cambia.
 * role: "ADMIN" (todo), "STAFF" (trabajador: todo menos usuarios y configuración)
 * o "SELLER" (vendedor: solo registra ventas y ve las suyas, sin costos ni ganancias).
 */
public record UserRequest(
		@NotBlank(message = "Ingresa el nombre.") @Size(max = 100, message = "El nombre es demasiado largo.") String name,
		@Size(max = 255, message = "El usuario es demasiado largo.")
		@Pattern(regexp = "^\\s*[A-Za-z0-9._@+-]{3,}\\s*$|^$",
				message = "Usa al menos 3 caracteres: letras, números, punto, guion o @ (sin espacios).") String username,
		@Size(max = 72, message = "La contraseña puede tener hasta 72 caracteres.") String password,
		@NotBlank(message = "Elige un rol.")
		@Pattern(regexp = "ADMIN|STAFF|SELLER", message = "Rol no válido.") String role) {}
