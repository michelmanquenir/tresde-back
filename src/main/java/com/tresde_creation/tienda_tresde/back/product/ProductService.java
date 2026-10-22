package com.tresde_creation.tienda_tresde.back.product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tresde_creation.tienda_tresde.back.category.CategoryRepository;
import com.tresde_creation.tienda_tresde.back.common.ApiException;
import com.tresde_creation.tienda_tresde.back.common.Slugs;
import com.tresde_creation.tienda_tresde.back.image.ImageService;

@Service
public class ProductService {

	private final ProductRepository products;
	private final CategoryRepository categories;
	private final ImageService images;

	public ProductService(ProductRepository products, CategoryRepository categories, ImageService images) {
		this.products = products;
		this.categories = categories;
		this.images = images;
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
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse getVisible(String id) {
		return products.findByIdAndHiddenFalse(id).map(this::toResponse)
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
	public List<ProductResponse> replaceAll(List<ImportedProduct> items) {
		List<Product> current = products.findAll();
		Set<String> oldImages = new HashSet<>();
		current.forEach(p -> oldImages.addAll(p.getImages()));
		products.deleteAll(current);
		products.flush();

		Set<String> usedIds = new HashSet<>();
		List<Product> saved = new ArrayList<>();
		for (ImportedProduct item : items) {
			String base = item.id() != null && !item.id().isBlank() ? Slugs.slugify(item.id()) : Slugs.slugify(item.data().name());
			String id = base;
			for (int n = 2; usedIds.contains(id); n++) id = base + "-" + n;
			usedIds.add(id);
			Product product = new Product(id);
			product.setRating(item.rating());
			apply(product, item.data());
			saved.add(products.save(product));
		}

		Set<String> newImages = new HashSet<>();
		saved.forEach(p -> newImages.addAll(p.getImages()));
		oldImages.stream().filter(url -> !newImages.contains(url))
				.forEach(url -> images.storedId(url).ifPresent(images::delete));
		return saved.stream().map(this::toResponse).toList();
	}

	public record ImportedProduct(String id, Double rating, ProductRequest data) {}

	// ── Internos ──

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
	}

	/** Borra las fotos subidas que dejaron de usarse (y que ningún otro producto usa). */
	private void deleteUnusedImages(List<String> before, List<String> after, String productId) {
		for (String url : before) {
			if (after.contains(url) || products.isImageUsedByOther(url, productId)) continue;
			images.storedId(url).ifPresent(images::delete);
		}
	}

	private String uniqueId(String name) {
		String base = Slugs.slugify(name);
		String id = base;
		for (int n = 2; products.existsById(id); n++) id = base + "-" + n;
		return id;
	}

	private ProductResponse toResponse(Product p) {
		return new ProductResponse(
				p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getOldPrice(), p.getDescription(),
				p.getMaterial(), List.copyOf(p.getColors()),
				p.getImages().stream().map(ProductService::absoluteUrl).toList(),
				p.getHeightCm(), p.isNew(), p.isBestSeller(), p.isHidden(), p.getRating(), p.getPrintHours(),
				p.getCreatedAt(), p.getUpdatedAt());
	}

	/** Las fotos guardadas aquí se almacenan como /api/images/{id}; al frontend se le entrega la URL completa. */
	private static String absoluteUrl(String url) {
		if (!url.startsWith(ImageService.PATH_PREFIX)) return url;
		try {
			return ServletUriComponentsBuilder.fromCurrentContextPath().path(url).toUriString();
		} catch (IllegalStateException e) {
			return url; // fuera de una petición HTTP
		}
	}
}
