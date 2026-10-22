package com.tresde_creation.tienda_tresde.back.image;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredImageRepository extends JpaRepository<StoredImage, UUID> {}
