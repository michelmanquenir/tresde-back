package com.tresde_creation.tienda_tresde.back.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Línea del detalle de una entrega: un producto y cuántas unidades. */
@Embeddable
public class DeliveryItem {

	/** null si el producto se eliminó después de la entrega. */
	@Column(name = "product_id")
	private String productId;

	@Column(name = "product_name")
	private String productName;

	private int quantity;

	protected DeliveryItem() {}

	public DeliveryItem(String productId, String productName, int quantity) {
		this.productId = productId;
		this.productName = productName;
		this.quantity = quantity;
	}

	public String getProductId() { return productId; }
	public String getProductName() { return productName; }
	public int getQuantity() { return quantity; }
}
