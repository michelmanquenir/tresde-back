package com.tresde_creation.tienda_tresde.back.category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

	List<Category> findAllByOrderByPositionAsc();
}
