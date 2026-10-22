package com.tresde_creation.tienda_tresde.back.category;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	public record CategoryResponse(String slug, String name, String description, String icon, int hue) {}

	private final CategoryRepository categories;

	public CategoryController(CategoryRepository categories) {
		this.categories = categories;
	}

	@GetMapping
	List<CategoryResponse> list() {
		return categories.findAllByOrderByPositionAsc().stream()
				.map(c -> new CategoryResponse(c.getSlug(), c.getName(), c.getDescription(), c.getIcon(), c.getHue()))
				.toList();
	}
}
