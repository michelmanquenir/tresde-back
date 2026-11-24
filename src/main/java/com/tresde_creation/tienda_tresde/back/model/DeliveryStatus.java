package com.tresde_creation.tienda_tresde.back.model;

/** Estado de una entrega de inventario. Solo PENDING puede cambiar. */
public enum DeliveryStatus {
	/** Entregada, esperando que quien recibe la acepte. */
	PENDING,
	/** Aceptada: el stock ya se sumó. */
	ACCEPTED,
	/** Quien recibe la rechazó (no se sumó nada). */
	REJECTED,
	/** Quien entregó la anuló antes de que la aceptaran. */
	CANCELLED
}
