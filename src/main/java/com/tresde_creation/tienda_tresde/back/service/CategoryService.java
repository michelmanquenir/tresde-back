package com.tresde_creation.tienda_tresde.back.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.CategoryAdminResponse;
import com.tresde_creation.tienda_tresde.back.dto.CategoryRequest;
import com.tresde_creation.tienda_tresde.back.dto.CategoryResponse;
import com.tresde_creation.tienda_tresde.back.model.Category;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.repository.CategoryRepository;
import com.tresde_creation.tienda_tresde.back.repository.ProductRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;
import com.tresde_creation.tienda_tresde.back.utils.Slugs;

@Service
public class CategoryService {

	private static final int MAX_SLUG = 40;

	private final CategoryRepository categories;
	private final ProductRepository products;

	public CategoryService(CategoryRepository categories, ProductRepository products) {
		this.categories = categories;
		this.products = products;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> list() {
		return categories.findAllByOrderByPositionAsc().stream()
				.map(c -> new CategoryResponse(c.getSlug(), c.getName(), c.getDescription(), c.getIcon(), c.getHue()))
				.toList();
	}

	// ── Panel ──

	@Transactional(readOnly = true)
	public List<CategoryAdminResponse> adminList() {
		Map<String, Integer> counts = new HashMap<>();
		products.findAll().forEach(p -> p.getCategories().forEach(s -> counts.merge(s, 1, Integer::sum)));
		return categories.findAllByOrderByPositionAsc().stream()
				.map(c -> toAdmin(c, counts.getOrDefault(c.getSlug(), 0))).toList();
	}

	@Transactional
	public CategoryAdminResponse create(CategoryRequest req) {
		String name = req.name().trim();
		checkName(name, null);
		String base = Slugs.slugify(name);
		if (base.length() > MAX_SLUG - 3) base = base.substring(0, MAX_SLUG - 3).replaceAll("-+$", "");
		String slug = base;
		for (int n = 2; categories.existsById(slug) || "todos".equals(slug); n++) slug = base + "-" + n;
		int position = categories.findAllByOrderByPositionAsc().stream().mapToInt(Category::getPosition).max().orElse(0) + 1;
		Category c = new Category(slug, position);
		apply(c, req);
		return toAdmin(categories.save(c), 0);
	}

	@Transactional
	public CategoryAdminResponse update(String slug, CategoryRequest req) {
		Category c = find(slug);
		checkName(req.name().trim(), slug);
		apply(c, req);
		long count = products.findAll().stream().filter(p -> p.getCategories().contains(slug)).count();
		return toAdmin(c, (int) count);
	}

	/** Nuevo orden del menú de la tienda: lista de slugs (las que falten quedan al final). */
	@Transactional
	public List<CategoryAdminResponse> reorder(List<String> slugs) {
		List<Category> all = new ArrayList<>(categories.findAllByOrderByPositionAsc());
		int pos = 1;
		for (String slug : slugs == null ? List.<String>of() : slugs) {
			for (Category c : all) {
				if (c.getSlug().equals(slug)) {
					c.setPosition(pos++);
					all.remove(c);
					break;
				}
			}
		}
		for (Category c : all) c.setPosition(pos++);
		categories.flush();
		return adminList();
	}

	/**
	 * Elimina la categoría y la quita de los productos que la tenían junto a otras.
	 * Si algún producto la tiene como única categoría, no se elimina (quedaría sin categoría).
	 */
	@Transactional
	public void delete(String slug) {
		Category c = find(slug);
		List<Product> using = products.findAll().stream().filter(p -> p.getCategories().contains(slug)).toList();
		List<String> orphans = using.stream().filter(p -> p.getCategories().size() == 1).map(Product::getName).toList();
		if (!orphans.isEmpty()) {
			String names = String.join(", ", orphans.subList(0, Math.min(3, orphans.size())))
					+ (orphans.size() > 3 ? " y " + (orphans.size() - 3) + " más" : "");
			throw new ApiException(HttpStatus.CONFLICT, (orphans.size() == 1
					? "1 producto solo tiene esta categoría (" + names + ")."
					: orphans.size() + " productos solo tienen esta categoría (" + names + ").")
					+ " Asígnales otra categoría antes de eliminarla.");
		}
		for (Product p : using) {
			p.setCategories(p.getCategories().stream().filter(s -> !s.equals(slug)).toList());
		}
		products.flush();
		categories.delete(c);
	}

	private void apply(Category c, CategoryRequest req) {
		c.setName(req.name().trim());
		c.setDescription(req.description() == null ? "" : req.description().trim());
		c.setIcon(req.icon());
		c.setHue(req.hue());
	}

	private void checkName(String name, String exceptSlug) {
		boolean taken = categories.findAll().stream()
				.anyMatch(c -> c.getName().equalsIgnoreCase(name) && !c.getSlug().equals(exceptSlug));
		if (taken) {
			String msg = "Ya existe una categoría con ese nombre.";
			throw new ApiException(HttpStatus.CONFLICT, msg, Map.of("name", msg));
		}
	}

	private Category find(String slug) {
		return categories.findById(slug).orElseThrow(() -> ApiException.notFound("Categoría no encontrada."));
	}

	private static CategoryAdminResponse toAdmin(Category c, int count) {
		return new CategoryAdminResponse(c.getSlug(), c.getName(), c.getDescription(), c.getIcon(), c.getHue(), count);
	}
}
