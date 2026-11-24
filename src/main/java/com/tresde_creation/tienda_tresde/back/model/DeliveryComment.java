package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Comentario dentro de una entrega (de quien entrega, de quien recibe o de un administrador). */
@Entity
@Table(name = "delivery_comments")
public class DeliveryComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "delivery_id")
	private Long deliveryId;

	@Column(name = "author_username")
	private String authorUsername;

	@Column(name = "author_name")
	private String authorName;

	private String body;

	@Column(name = "created_at")
	private Instant createdAt;

	protected DeliveryComment() {}

	public DeliveryComment(Long deliveryId, User author, String body) {
		this.deliveryId = deliveryId;
		this.authorUsername = author.getUsername();
		this.authorName = author.getName();
		this.body = body;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public Long getDeliveryId() { return deliveryId; }
	public String getAuthorUsername() { return authorUsername; }
	public String getAuthorName() { return authorName; }
	public String getBody() { return body; }
	public Instant getCreatedAt() { return createdAt; }
}
