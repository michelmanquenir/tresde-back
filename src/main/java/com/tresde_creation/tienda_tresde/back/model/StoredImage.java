package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Foto subida desde el panel. Se sirve en GET /api/images/{id}. */
@Entity
@Table(name = "stored_images")
public class StoredImage {

	@Id
	private UUID id;

	@Column(name = "content_type")
	private String contentType;

	private byte[] data;

	@Column(name = "size_bytes")
	private int sizeBytes;

	@Column(name = "created_at")
	private Instant createdAt;

	protected StoredImage() {}

	public StoredImage(String contentType, byte[] data) {
		this.id = UUID.randomUUID();
		this.contentType = contentType;
		this.data = data;
		this.sizeBytes = data.length;
		this.createdAt = Instant.now();
	}

	public UUID getId() { return id; }
	public String getContentType() { return contentType; }
	public byte[] getData() { return data; }
	public int getSizeBytes() { return sizeBytes; }
	public Instant getCreatedAt() { return createdAt; }
}
