package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Cuadratura (cierre semanal): agrupa las ventas pendientes hasta un corte y registra
 * cuánto se retiró de ganancia y de máquina, y cuánto se reinvirtió en materia prima.
 */
@Entity
@Table(name = "closings")
public class Closing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "period_from")
	private Instant periodFrom;

	@Column(name = "period_to")
	private Instant periodTo;

	@Column(name = "sales_count")
	private int salesCount;

	private int units;
	private double revenue;
	private double materials;
	private double machine;
	private double profit;

	@Column(name = "materials_reinvested")
	private double materialsReinvested;

	@Column(name = "machine_withdrawn")
	private double machineWithdrawn;

	@Column(name = "profit_withdrawn")
	private double profitWithdrawn;

	private String note;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_at")
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public Instant getPeriodFrom() { return periodFrom; }
	public void setPeriodFrom(Instant v) { this.periodFrom = v; }
	public Instant getPeriodTo() { return periodTo; }
	public void setPeriodTo(Instant v) { this.periodTo = v; }
	public int getSalesCount() { return salesCount; }
	public void setSalesCount(int v) { this.salesCount = v; }
	public int getUnits() { return units; }
	public void setUnits(int v) { this.units = v; }
	public double getRevenue() { return revenue; }
	public void setRevenue(double v) { this.revenue = v; }
	public double getMaterials() { return materials; }
	public void setMaterials(double v) { this.materials = v; }
	public double getMachine() { return machine; }
	public void setMachine(double v) { this.machine = v; }
	public double getProfit() { return profit; }
	public void setProfit(double v) { this.profit = v; }
	public double getMaterialsReinvested() { return materialsReinvested; }
	public void setMaterialsReinvested(double v) { this.materialsReinvested = v; }
	public double getMachineWithdrawn() { return machineWithdrawn; }
	public void setMachineWithdrawn(double v) { this.machineWithdrawn = v; }
	public double getProfitWithdrawn() { return profitWithdrawn; }
	public void setProfitWithdrawn(double v) { this.profitWithdrawn = v; }
	public String getNote() { return note; }
	public void setNote(String v) { this.note = v; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String v) { this.createdBy = v; }
	public Instant getCreatedAt() { return createdAt; }
}
