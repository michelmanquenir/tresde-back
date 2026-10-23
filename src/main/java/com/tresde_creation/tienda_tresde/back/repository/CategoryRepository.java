package com.tresde_creation.tienda_tresde.back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tresde_creation.tienda_tresde.back.model.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {

	List<Category> findAllByOrderByPositionAsc();
}
