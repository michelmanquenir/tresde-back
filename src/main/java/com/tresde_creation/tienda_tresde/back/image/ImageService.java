package com.tresde_creation.tienda_tresde.back.image;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.common.ApiException;

@Service
public class ImageService {

	/** Ruta (relativa) con la que se guardan las fotos subidas dentro de los productos. */
	public static final String PATH_PREFIX = "/api/images/";

	private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
	private static final int MAX_BYTES = 5 * 1024 * 1024;
	private static final Pattern DATA_URL = Pattern.compile("^data:(image/[a-z+]+);base64,(.+)$", Pattern.DOTALL);
	private static final Pattern STORED_PATH = Pattern.compile("/api/images/([0-9a-fA-F-]{36})");

	private final StoredImageRepository images;

	public ImageService(StoredImageRepository images) {
		this.images = images;
	}

	@Transactional
	public StoredImage store(String contentType, byte[] data) {
		if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
			throw ApiException.badRequest("Formato de imagen no soportado. Usa JPG, PNG, WEBP o GIF.");
		}
		if (data == null || data.length == 0) throw ApiException.badRequest("La imagen está vacía.");
		if (data.length > MAX_BYTES) throw ApiException.badRequest("La imagen es demasiado grande (máximo 5 MB).");
		return images.save(new StoredImage(contentType.toLowerCase(), data));
	}

	@Transactional(readOnly = true)
	public Optional<StoredImage> find(UUID id) {
		return images.findById(id);
	}

	@Transactional
	public void delete(UUID id) {
		images.deleteById(id);
	}

	/**
	 * Normaliza una URL de imagen recibida del frontend:
	 * - data:image/...;base64 -> se guarda en la base y se devuelve /api/images/{id}
	 * - http(s)://host/api/images/{id} -> /api/images/{id} (se guarda relativa)
	 * - cualquier otra URL se deja igual
	 */
	@Transactional
	public String normalizeUrl(String url) {
		String value = url.trim();
		Matcher data = DATA_URL.matcher(value);
		if (data.matches()) {
			byte[] bytes;
			try {
				bytes = Base64.getDecoder().decode(data.group(2).trim());
			} catch (IllegalArgumentException e) {
				throw ApiException.badRequest("Una de las imágenes no es válida.");
			}
			return PATH_PREFIX + store(data.group(1), bytes).getId();
		}
		if (value.length() > 1000) throw ApiException.badRequest("La URL de una imagen es demasiado larga.");
		return storedId(value).map(id -> PATH_PREFIX + id).orElse(value);
	}

	/** Si la URL apunta a una foto guardada aquí, devuelve su id. */
	public Optional<UUID> storedId(String url) {
		Matcher m = STORED_PATH.matcher(url);
		if (!m.find()) return Optional.empty();
		try {
			return Optional.of(UUID.fromString(m.group(1)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
