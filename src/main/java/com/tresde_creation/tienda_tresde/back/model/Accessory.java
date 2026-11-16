package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Insumo que se agrega a una pieza impresa (argolla de llavero, mecanismo de clicker, imán…). */
@Entity
@Table(name = "accessories")
public class Accessory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	/** Costo por unidad, en pesos. */
	private int cost;

	@Column(name = "created_at")
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public int getCost() { return cost; }
	public void setCost(int cost) { this.cost = cost; }
	public Instant getCreatedAt() { return createdAt; }
}
