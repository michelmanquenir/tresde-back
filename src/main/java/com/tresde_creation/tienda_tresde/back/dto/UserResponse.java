package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

public record UserResponse(Long id, String username, String name, String role, Instant createdAt) {}
