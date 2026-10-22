package com.tresde_creation.tienda_tresde.back.category;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

	@Id
	private String slug;
	private String name;
	private String description;
	private String icon;
	private int hue;
	private int position;

	protected Category() {}

	public String getSlug() { return slug; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getIcon() { return icon; }
	public int getHue() { return hue; }
	public int getPosition() { return position; }
}
