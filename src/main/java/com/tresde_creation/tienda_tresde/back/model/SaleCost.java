package com.tresde_creation.tienda_tresde.back.model;

/** Costo de una unidad: materia prima (filamento y accesorios) y electricidad. */
public record SaleCost(double filament, double accessories, double electricity) {

	public double total() {
		return filament + accessories + electricity;
	}
}
