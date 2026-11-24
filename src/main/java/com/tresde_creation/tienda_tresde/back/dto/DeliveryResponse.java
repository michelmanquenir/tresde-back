package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;
import java.util.List;

/** Entrega de inventario con su detalle y sus comentarios (del más antiguo al más nuevo). */
public record DeliveryResponse(
		Long id,
		String senderUsername,
		String senderName,
		String recipientUsername,
		String recipientName,
		String status,
		String note,
		int totalUnits,
		List<Item> items,
		List<Comment> comments,
		Instant deliveredAt,
		Instant resolvedAt,
		String resolvedBy,
		Instant createdAt) {

	/** productId es null si el producto se eliminó. */
	public record Item(String productId, String productName, int quantity) {}

	public record Comment(Long id, String authorUsername, String authorName, String body, Instant createdAt) {}

	/** Usuario del panel al que se le puede entregar. */
	public record Recipient(String username, String name, String role) {}
}
