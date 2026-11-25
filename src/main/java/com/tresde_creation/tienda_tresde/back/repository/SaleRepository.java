package com.tresde_creation.tienda_tresde.back.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tresde_creation.tienda_tresde.back.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	/** Ventas en [from, to), las más recientes primero. */
	List<Sale> findBySoldAtGreaterThanEqualAndSoldAtLessThanOrderBySoldAtDescIdDesc(Instant from, Instant to);

	/** Ventas de un vendedor en [from, to), las más recientes primero. */
	List<Sale> findBySoldByIgnoreCaseAndSoldAtGreaterThanEqualAndSoldAtLessThanOrderBySoldAtDescIdDesc(
			String soldBy, Instant from, Instant to);

	/** Ventas pendientes de cuadratura vendidas antes del corte. */
	List<Sale> findByClosingIdIsNullAndSoldAtLessThanOrderBySoldAtAscIdAsc(Instant to);

	List<Sale> findByClosingId(Long closingId);

	long countByClosingIdIsNull();
}
