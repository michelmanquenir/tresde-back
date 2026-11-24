package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entrega de inventario de un usuario del panel a otro. El stock de los productos
 * recién se suma cuando quien recibe la acepta.
 */
@Entity
@Table(name = "deliveries")
public class Delivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sender_username")
	private String senderUsername;

	@Column(name = "sender_name")
	private String senderName;

	@Column(name = "recipient_username")
	private String recipientUsername;

	@Column(name = "recipient_name")
	private String recipientName;

	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;

	private String note;

	@Column(name = "delivered_at")
	private Instant deliveredAt;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@Column(name = "resolved_by")
	private String resolvedBy;

	@Column(name = "created_at")
	private Instant createdAt;

	@ElementCollection
	@CollectionTable(name = "delivery_items", joinColumns = @JoinColumn(name = "delivery_id"))
	@OrderColumn(name = "position")
	@BatchSize(size = 100)
	private List<DeliveryItem> items = new ArrayList<>();

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		if (deliveredAt == null) deliveredAt = createdAt;
		if (status == null) status = DeliveryStatus.PENDING;
	}

	/** Cierra la entrega (aceptada, rechazada o cancelada). */
	public void resolve(DeliveryStatus status, String byUsername) {
		this.status = status;
		this.resolvedAt = Instant.now();
		this.resolvedBy = byUsername;
	}

	public int totalUnits() {
		return items.stream().mapToInt(DeliveryItem::getQuantity).sum();
	}

	public boolean involves(String username) {
		return senderUsername.equalsIgnoreCase(username) || recipientUsername.equalsIgnoreCase(username);
	}

	public Long getId() { return id; }
	public String getSenderUsername() { return senderUsername; }
	public void setSenderUsername(String v) { this.senderUsername = v; }
	public String getSenderName() { return senderName; }
	public void setSenderName(String v) { this.senderName = v; }
	public String getRecipientUsername() { return recipientUsername; }
	public void setRecipientUsername(String v) { this.recipientUsername = v; }
	public String getRecipientName() { return recipientName; }
	public void setRecipientName(String v) { this.recipientName = v; }
	public DeliveryStatus getStatus() { return status; }
	public String getNote() { return note; }
	public void setNote(String v) { this.note = v; }
	public Instant getDeliveredAt() { return deliveredAt; }
	public void setDeliveredAt(Instant v) { this.deliveredAt = v; }
	public Instant getResolvedAt() { return resolvedAt; }
	public String getResolvedBy() { return resolvedBy; }
	public Instant getCreatedAt() { return createdAt; }
	public List<DeliveryItem> getItems() { return items; }
}
