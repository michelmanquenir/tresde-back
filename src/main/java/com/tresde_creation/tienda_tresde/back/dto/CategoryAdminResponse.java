package com.tresde_creation.tienda_tresde.back.dto;

/** Categoría para el panel: incluye cuántos productos la usan (visibles u ocultos). */
public record CategoryAdminResponse(String slug, String name, String description, String icon, int hue, int products) {}
