package com.tresde_creation.tienda_tresde.back.dto;

/** Accesorio del catálogo; usedBy = cuántos productos lo llevan. */
public record AccessoryResponse(Long id, String name, int cost, long usedBy) {}
