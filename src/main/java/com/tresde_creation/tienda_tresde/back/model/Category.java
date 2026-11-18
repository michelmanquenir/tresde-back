package com.tresde_creation.tienda_tresde.back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

	/** Se genera del nombre al crearla y no cambia (va en la URL: /categoria/{slug}). */
	@Id
	private String slug;
	private String name;
	private String description;
	private String icon;
	private int hue;
	private int position;

	protected Category() {}

	public Category(String slug, int position) {
		this.slug = slug;
		this.position = position;
	}

	public String getSlug() { return slug; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getIcon() { return icon; }
	public void setIcon(String icon) { this.icon = icon; }
	public int getHue() { return hue; }
	public void setHue(int hue) { this.hue = hue; }
	public int getPosition() { return position; }
	public void setPosition(int position) { this.position = position; }
}
