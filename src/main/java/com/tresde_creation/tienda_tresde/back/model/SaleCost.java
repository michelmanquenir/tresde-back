package com.tresde_creation.tienda_tresde.back.model;

/** Costo de una unidad vendida: materia prima (filamento + accesorios). La luz no se cuenta en las ventas. */
public record SaleCost(double filament, double accessories) {

	public double total() {
		return filament + accessories;
	}
}
