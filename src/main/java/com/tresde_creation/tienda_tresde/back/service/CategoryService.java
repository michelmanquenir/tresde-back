package com.tresde_creation.tienda_tresde.back.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.CategoryResponse;
import com.tresde_creation.tienda_tresde.back.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categories;

	public CategoryService(CategoryRepository categories) {
		this.categories = categories;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> list() {
		return categories.findAllByOrderByPositionAsc().stream()
				.map(c -> new CategoryResponse(c.getSlug(), c.getName(), c.getDescription(), c.getIcon(), c.getHue()))
				.toList();
	}
}
