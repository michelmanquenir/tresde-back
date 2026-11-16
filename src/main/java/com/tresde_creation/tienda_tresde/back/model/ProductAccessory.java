package com.tresde_creation.tienda_tresde.back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Un accesorio que lleva la pieza y cuántas unidades de él. */
@Embeddable
public class ProductAccessory {

	@Column(name = "accessory_id")
	private Long accessoryId;

	private int quantity;

	protected ProductAccessory() {}

	public ProductAccessory(Long accessoryId, int quantity) {
		this.accessoryId = accessoryId;
		this.quantity = quantity;
	}

	public Long getAccessoryId() { return accessoryId; }
	public int getQuantity() { return quantity; }
}
