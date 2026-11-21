package com.tresde_creation.tienda_tresde.back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tresde_creation.tienda_tresde.back.model.Closing;

public interface ClosingRepository extends JpaRepository<Closing, Long> {

	List<Closing> findAllByOrderByPeriodToDescIdDesc();

	@Query("select coalesce(sum(c.machineWithdrawn), 0) from Closing c")
	double totalMachineWithdrawn();
}
