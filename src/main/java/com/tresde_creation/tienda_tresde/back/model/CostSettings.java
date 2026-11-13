package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Configuración global de costos. Hay una sola fila (id = 1), creada por la migración V4. */
@Entity
@Table(name = "cost_settings")
public class CostSettings {

	public static final int SINGLETON_ID = 1;

	@Id
	private Integer id;

	@Column(name = "machine_hour_cost")
	private double machineHourCost;

	@Column(name = "machine_reserve_percent")
	private double machineReservePercent;

	@Column(name = "price_multiplier")
	private double priceMultiplier;

	@Column(name = "filament_pla")
	private int filamentPla;

	@Column(name = "filament_petg")
	private int filamentPetg;

	@Column(name = "filament_resina")
	private int filamentResina;

	@Column(name = "filament_tpu")
	private int filamentTpu;

	@Column(name = "updated_at")
	private Instant updatedAt;

	protected CostSettings() {}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public double getMachineHourCost() { return machineHourCost; }
	public void setMachineHourCost(double v) { this.machineHourCost = v; }
	public double getMachineReservePercent() { return machineReservePercent; }
	public void setMachineReservePercent(double v) { this.machineReservePercent = v; }
	public double getPriceMultiplier() { return priceMultiplier; }
	public void setPriceMultiplier(double v) { this.priceMultiplier = v; }

	public int getFilamentPrice(Material m) {
		return switch (m) {
			case PLA -> filamentPla;
			case PETG -> filamentPetg;
			case Resina -> filamentResina;
			case TPU -> filamentTpu;
		};
	}

	public void setFilamentPrice(Material m, int pricePerKg) {
		switch (m) {
			case PLA -> filamentPla = pricePerKg;
			case PETG -> filamentPetg = pricePerKg;
			case Resina -> filamentResina = pricePerKg;
			case TPU -> filamentTpu = pricePerKg;
		}
	}
}
