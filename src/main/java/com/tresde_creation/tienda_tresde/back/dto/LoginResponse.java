package com.tresde_creation.tienda_tresde.back.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt, UserInfoResponse user) {}
