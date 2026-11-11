package com.tresde_creation.tienda_tresde.back.dto;

/** role: "ADMIN" (panel de administración) o "CUSTOMER" (cliente de la tienda). */
public record UserInfoResponse(String username, String name, String role) {}
