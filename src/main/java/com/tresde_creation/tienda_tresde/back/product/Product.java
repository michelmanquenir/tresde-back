package com.tresde_creation.tienda_tresde.back.product;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	private String id;

	private String name;

	@Column(name = "category_slug")
	private String category;

	private Integer price;

	@Column(name = "old_price")
	private Integer oldPrice;

	private String description;

	@Enumerated(EnumType.STRING)
	private Material material;

	@Column(name = "height_cm")
	private Double heightCm;

	@Column(name = "print_hours")
	private Double printHours;

	private Double rating;

	@Column(name = "is_new")
	private boolean isNew;

	@Column(name = "best_seller")
	private boolean bestSeller;

	private boolean hidden;

	@ElementCollection
	@CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
	@OrderColumn(name = "position")
	@Column(name = "color")
	private List<String> colors = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
	@OrderColumn(name = "position")
	@Column(name = "url")
	private List<String> images = new ArrayList<>();

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	protected Product() {}

	public Product(String id) {
		this.id = id;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
		updatedAt = Instant.now();
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public String getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public Integer getPrice() { return price; }
	public void setPrice(Integer price) { this.price = price; }
	public Integer getOldPrice() { return oldPrice; }
	public void setOldPrice(Integer oldPrice) { this.oldPrice = oldPrice; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Material getMaterial() { return material; }
	public void setMaterial(Material material) { this.material = material; }
	public Double getHeightCm() { return heightCm; }
	public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
	public Double getPrintHours() { return printHours; }
	public void setPrintHours(Double printHours) { this.printHours = printHours; }
	public Double getRating() { return rating; }
	public void setRating(Double rating) { this.rating = rating; }
	public boolean isNew() { return isNew; }
	public void setNew(boolean isNew) { this.isNew = isNew; }
	public boolean isBestSeller() { return bestSeller; }
	public void setBestSeller(boolean bestSeller) { this.bestSeller = bestSeller; }
	public boolean isHidden() { return hidden; }
	public void setHidden(boolean hidden) { this.hidden = hidden; }
	public List<String> getColors() { return colors; }
	public void setColors(List<String> colors) { this.colors.clear(); this.colors.addAll(colors); }
	public List<String> getImages() { return images; }
	public void setImages(List<String> images) { this.images.clear(); this.images.addAll(images); }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
