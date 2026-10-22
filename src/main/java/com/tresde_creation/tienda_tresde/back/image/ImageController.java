package com.tresde_creation.tienda_tresde.back.image;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tresde_creation.tienda_tresde.back.common.ApiException;

@RestController
public class ImageController {

	public record UploadResponse(String id, String url) {}

	private final ImageService images;

	public ImageController(ImageService images) {
		this.images = images;
	}

	/** Público: devuelve la foto. Se cachea un año porque cada id es único e inmutable. */
	@GetMapping("/api/images/{id}")
	ResponseEntity<byte[]> get(@PathVariable UUID id) {
		StoredImage img = images.find(id).orElseThrow(() -> ApiException.notFound("Imagen no encontrada."));
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(img.getContentType()))
				.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
				.body(img.getData());
	}

	/** Admin: sube una foto (multipart, campo "file") y devuelve su URL. */
	@PostMapping("/api/admin/images")
	@ResponseStatus(HttpStatus.CREATED)
	UploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
		StoredImage img = images.store(file.getContentType(), file.getBytes());
		String url = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(ImageService.PATH_PREFIX + img.getId())
				.toUriString();
		return new UploadResponse(img.getId().toString(), url);
	}
}
