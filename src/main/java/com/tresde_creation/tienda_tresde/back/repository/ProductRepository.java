package com.tresde_creation.tienda_tresde.back.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tresde_creation.tienda_tresde.back.model.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

	@EntityGraph(attributePaths = { "colors", "images" })
	@Query("select p from Product p order by p.createdAt desc")
	List<Product> findAllNewestFirst();

	@EntityGraph(attributePaths = { "colors", "images" })
	@Query("select p from Product p where p.hidden = false order by p.createdAt desc")
	List<Product> findVisibleNewestFirst();

	@EntityGraph(attributePaths = { "colors", "images" })
	Optional<Product> findByIdAndHiddenFalse(String id);

	/** ¿Algún producto (distinto del indicado) usa esta URL de imagen? */
	@Query("select count(p) > 0 from Product p join p.images i where i = :url and p.id <> :excludeId")
	boolean isImageUsedByOther(@Param("url") String url, @Param("excludeId") String excludeId);
}
