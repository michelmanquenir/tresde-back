package com.tresde_creation.tienda_tresde.back.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tresde_creation.tienda_tresde.back.model.StoredImage;

public interface StoredImageRepository extends JpaRepository<StoredImage, UUID> {}
