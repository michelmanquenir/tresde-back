package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.Size;

/** Comentario de una entrega. Al aceptar, rechazar o cancelar es opcional; al comentar, obligatorio. */
public record DeliveryCommentRequest(
		@Size(max = 500, message = "Máximo 500 caracteres.")
		String comment) {}
