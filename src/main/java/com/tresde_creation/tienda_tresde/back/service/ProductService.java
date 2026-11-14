package com.tresde_creation.tienda_tresde.back.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.ImportProductRequest;
import com.tresde_creation.tienda_tresde.back.dto.ProductCost;
import com.tresde_creation.tienda_tresde.back.dto.ProductRequest;
import com.tresde_creation.tienda_tresde.back.dto.ProductResponse;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.repository.CategoryRepository;
import com.tresde_creation.tienda_tresde.back.repository.ProductRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;
import com.tresde_creation.tienda_tresde.back.utils.ImageUrls;
import com.tresde_creation.tienda_tresde.back.utils.Slugs;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class ProductService {

	private final ProductRepository products;
	private final CategoryRepository categories;
	private final ImageService images;
	private final Validator validator;

	public ProductService(ProductRepository products, CategoryRepository categories, ImageService images,
			Validator validator) {
		this.products = products;
		this.categories = categories;
		this.images = images;
		this.validator = validator;
	}

	// ── Lectura ──

	@Transactional(readOnly = true)
	public List<ProductResponse> listVisible(String category, String query) {
		String q = query == null ? "" : query.trim().toLowerCase();
		return products.findVisibleNewestFirst().stream()
				.filter(p -> category == null || category.isBlank() || p.getCategory().equals(category))
				.filter(p -> q.isEmpty()
						|| p.getName().toLowerCase().contains(q)
						|| p.getDescription().toLowerCase().contains(q))
				.map(this::toPublicResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse getVisible(String id) {
		return products.findByIdAndHiddenFalse(id).map(this::toPublicResponse)
				.orElseThrow(() -> ApiException.notFound("Producto no encontrado."));
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> listAll() {
		return products.findAllNewestFirst().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse get(String id) {
		return toResponse(find(id));
	}

	// ── Escritura (panel) ──

	@Transactional
	public ProductResponse create(ProductRequest req) {
		Product product = new Product(uniqueId(req.name()));
		apply(product, req);
		return toResponse(products.save(product));
	}

	@Transactional
	public ProductResponse update(String id, ProductRequest req) {
		Product product = find(id);
		List<String> before = List.copyOf(product.getImages());
		apply(product, req);
		deleteUnusedImages(before, product.getImages(), id);
		return toResponse(product);
	}

	@Transactional
	public ProductResponse setStock(String id, int stock) {
		Product product = find(id);
		product.setStock(stock);
		return toResponse(product);
	}

	@Transactional
	public ProductResponse setHidden(String id, boolean hidden) {
		Product product = find(id);
		product.setHidden(hidden);
		return toResponse(product);
	}

	@Transactional
	public void delete(String id) {
		Product product = find(id);
		List<String> before = List.copyOf(product.getImages());
		products.delete(product);
		deleteUnusedImages(before, List.of(), id);
	}

	/** Reemplaza todo el catálogo (para importar un respaldo JSON exportado desde el panel). */
	@Transactional
	public List<ProductResponse> replaceAll(List<ImportProductRequest> items) {
		if (items == null || items.isEmpty()) throw ApiException.badRequest("El archivo no contiene productos.");
		validateImport(items);

		List<Product> current = products.findAll();
		Set<String> oldImages = new HashSet<>();
		current.forEach(p -> oldImages.addAll(p.getImages()));
		products.deleteAll(current);
		products.flush();

		Set<String> usedIds = new HashSet<>();
		List<Product> saved = new ArrayList<>();
		for (ImportProductRequest item : items) {
			String base = Slugs.slugify(item.id() != null && !item.id().isBlank() ? item.id() : item.name());
			String id = base;
			for (int n = 2; usedIds.contains(id); n++) id = base + "-" + n;
			usedIds.add(id);
			Product product = new Product(id);
			product.setRating(item.rating());
			apply(product, item.toProductRequest());
			saved.add(products.save(product));
		}

		Set<String> newImages = new HashSet<>();
		saved.forEach(p -> newImages.addAll(p.getImages()));
		oldImages.stream().filter(url -> !newImages.contains(url)).forEach(images::deleteByUrl);
		return saved.stream().map(this::toResponse).toList();
	}

	// ── Internos ──

	private void validateImport(List<ImportProductRequest> items) {
		for (int i = 0; i < items.size(); i++) {
			ImportProductRequest item = items.get(i);
			Set<ConstraintViolation<ProductRequest>> violations = validator.validate(item.toProductRequest());
			if (!violations.isEmpty()) {
				ConstraintViolation<ProductRequest> v = violations.iterator().next();
				String label = item.name() != null ? "\"" + item.name() + "\"" : "#" + (i + 1);
				throw ApiException.badRequest("Producto " + label + ": " + v.getPropertyPath() + " - " + v.getMessage());
			}
		}
	}

	private Product find(String id) {
		return products.findById(id).orElseThrow(() -> ApiException.notFound("Producto no encontrado."));
	}

	private void apply(Product p, ProductRequest req) {
		if (!categories.existsById(req.category())) {
			throw ApiException.badRequest("La categoría \"" + req.category() + "\" no existe.");
		}
		if (req.oldPrice() != null && req.oldPrice() <= req.price()) {
			throw ApiException.badRequest("El precio anterior debe ser mayor al precio actual.");
		}
		p.setName(req.name().trim());
		p.setCategory(req.category());
		p.setPrice(req.price());
		p.setOldPrice(req.oldPrice());
		p.setDescription(req.description().trim());
		p.setMaterial(req.material());
		p.setHeightCm(req.heightCm());
		p.setPrintHours(req.printHours());
		p.setColors(req.colors().stream().map(String::toLowerCase).toList());
		p.setImages(req.images() == null ? List.of() : req.images().stream().map(images::normalizeUrl).toList());
		p.setNew(Boolean.TRUE.equals(req.isNew()));
		p.setBestSeller(Boolean.TRUE.equals(req.bestSeller()));
		p.setHidden(Boolean.TRUE.equals(req.hidden()));
		p.setStock(req.stock());
		ProductCost cost = req.cost();
		p.setGrams(cost == null ? null : cost.grams());
		p.setBatchHours(cost == null ? null : cost.batchHours());
		p.setBatchPieces(cost == null ? null : cost.batchPieces());
		p.setAccessoriesCost(cost == null ? null : cost.accessoriesCost());
	}

	/** Borra las fotos subidas que dejaron de usarse (y que ningún otro producto usa). */
	private void deleteUnusedImages(List<String> before, List<String> after, String productId) {
		for (String url : before) {
			if (after.contains(url) || products.isImageUsedByOther(url, productId)) continue;
			images.deleteByUrl(url);
		}
	}

	private String uniqueId(String name) {
		String base = Slugs.slugify(name);
		String id = base;
		for (int n = 2; products.existsById(id); n++) id = base + "-" + n;
		return id;
	}

	/** Para el panel: incluye los datos de costo. */
	private ProductResponse toResponse(Product p) {
		return toResponse(p, new ProductCost(p.getGrams(), p.getBatchHours(), p.getBatchPieces(), p.getAccessoriesCost()));
	}

	/** Para la tienda: sin datos de costo (el campo "cost" no aparece en el JSON). */
	private ProductResponse toPublicResponse(Product p) {
		return toResponse(p, null);
	}

	private ProductResponse toResponse(Product p, ProductCost cost) {
		return new ProductResponse(
				p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getOldPrice(), p.getDescription(),
				p.getMaterial(), List.copyOf(p.getColors()),
				p.getImages().stream().map(ImageUrls::absolute).toList(),
				p.getHeightCm(), p.isNew(), p.isBestSeller(), p.isHidden(), p.getRating(), p.getPrintHours(), p.getStock(),
				p.getCreatedAt(), p.getUpdatedAt(), cost);
	}
}
