package com.tresde_creation.tienda_tresde.back.dto;

/** role: "ADMIN" (administrador), "STAFF" (trabajador) o "CUSTOMER" (cliente antiguo, sin acceso al panel). */
public record UserInfoResponse(String username, String name, String role) {}
