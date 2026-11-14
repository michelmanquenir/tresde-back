package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Venta registrada a mano por el administrador. */
@Entity
@Table(name = "sales")
public class Sale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_id")
	private String productId;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "category_slug")
	private String category;

	private String color;
	private int quantity;

	@Column(name = "unit_price")
	private int unitPrice;

	@Column(name = "unit_cost")
	private Double unitCost;

	@Column(name = "reserve_percent")
	private double reservePercent;

	@Column(name = "stock_discounted")
	private boolean stockDiscounted;

	private String channel;
	private String customer;
	private String note;

	@Column(name = "sold_at")
	private Instant soldAt;

	@Column(name = "created_at")
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		if (soldAt == null) soldAt = createdAt;
	}

	public Long getId() { return id; }
	public String getProductId() { return productId; }
	public void setProductId(String productId) { this.productId = productId; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public int getUnitPrice() { return unitPrice; }
	public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
	public Double getUnitCost() { return unitCost; }
	public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
	public double getReservePercent() { return reservePercent; }
	public void setReservePercent(double reservePercent) { this.reservePercent = reservePercent; }
	public boolean isStockDiscounted() { return stockDiscounted; }
	public void setStockDiscounted(boolean stockDiscounted) { this.stockDiscounted = stockDiscounted; }
	public String getChannel() { return channel; }
	public void setChannel(String channel) { this.channel = channel; }
	public String getCustomer() { return customer; }
	public void setCustomer(String customer) { this.customer = customer; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public Instant getSoldAt() { return soldAt; }
	public void setSoldAt(Instant soldAt) { this.soldAt = soldAt; }
	public Instant getCreatedAt() { return createdAt; }
}
