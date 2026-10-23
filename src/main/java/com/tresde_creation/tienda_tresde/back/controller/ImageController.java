package com.tresde_creation.tienda_tresde.back.controller;

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

import com.tresde_creation.tienda_tresde.back.dto.ImageUploadResponse;
import com.tresde_creation.tienda_tresde.back.model.StoredImage;
import com.tresde_creation.tienda_tresde.back.service.ImageService;
import com.tresde_creation.tienda_tresde.back.utils.ImageUrls;

@RestController
public class ImageController {

	private final ImageService imageService;

	public ImageController(ImageService imageService) {
		this.imageService = imageService;
	}

	/** Público: devuelve la foto. Se cachea un año porque cada id es único e inmutable. */
	@GetMapping("/api/images/{id}")
	public ResponseEntity<byte[]> get(@PathVariable UUID id) {
		StoredImage img = imageService.get(id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(img.getContentType()))
				.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
				.body(img.getData());
	}

	/** Admin: sube una foto (multipart, campo "file") y devuelve su URL. */
	@PostMapping("/api/admin/images")
	@ResponseStatus(HttpStatus.CREATED)
	public ImageUploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
		StoredImage img = imageService.store(file.getContentType(), file.getBytes());
		return new ImageUploadResponse(img.getId().toString(), ImageUrls.absolute(ImageUrls.relative(img.getId())));
	}
}
