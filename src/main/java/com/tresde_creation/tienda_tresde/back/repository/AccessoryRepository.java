package com.tresde_creation.tienda_tresde.back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tresde_creation.tienda_tresde.back.model.Accessory;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

	List<Accessory> findAllByOrderByNameAsc();

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	/** [accessory_id, cantidad de productos que lo usan] */
	@Query(value = "select accessory_id, count(*) from product_accessories group by accessory_id", nativeQuery = true)
	List<Object[]> countUsage();
}
