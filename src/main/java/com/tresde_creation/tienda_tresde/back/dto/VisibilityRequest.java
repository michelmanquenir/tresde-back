package com.tresde_creation.tienda_tresde.back.dto;

import jakarta.validation.constraints.NotNull;

public record VisibilityRequest(@NotNull(message = "Indica si el producto está oculto.") Boolean hidden) {}
