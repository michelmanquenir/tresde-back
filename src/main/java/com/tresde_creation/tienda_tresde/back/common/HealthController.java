package com.tresde_creation.tienda_tresde.back.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Para comprobar que la API está arriba (útil en el hosting). */
@RestController
public class HealthController {

	@GetMapping("/api/health")
	Map<String, String> health() {
		return Map.of("status", "ok");
	}
}
