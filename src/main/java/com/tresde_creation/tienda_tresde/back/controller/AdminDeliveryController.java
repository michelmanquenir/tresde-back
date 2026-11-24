package com.tresde_creation.tienda_tresde.back.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tresde_creation.tienda_tresde.back.dto.DeliveryCommentRequest;
import com.tresde_creation.tienda_tresde.back.dto.DeliveryRequest;
import com.tresde_creation.tienda_tresde.back.dto.DeliveryResponse;
import com.tresde_creation.tienda_tresde.back.service.DeliveryService;

import jakarta.validation.Valid;

/** Entregas de inventario entre usuarios del panel (ADMIN y STAFF, ver security/SecurityConfig). */
@RestController
@RequestMapping("/api/admin/deliveries")
public class AdminDeliveryController {

	private final DeliveryService service;

	public AdminDeliveryController(DeliveryService service) {
		this.service = service;
	}

	/** ?status=PENDING|ACCEPTED|REJECTED|CANCELLED (opcional). */
	@GetMapping
	public List<DeliveryResponse> list(@RequestParam(required = false) String status, @AuthenticationPrincipal Jwt jwt) {
		return service.list(jwt.getSubject(), status);
	}

	@GetMapping("/{id}")
	public DeliveryResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return service.get(id, jwt.getSubject());
	}

	@GetMapping("/pending-count")
	public Map<String, Long> pendingCount(@AuthenticationPrincipal Jwt jwt) {
		return Map.of("count", service.pendingForMe(jwt.getSubject()));
	}

	@GetMapping("/recipients")
	public List<DeliveryResponse.Recipient> recipients(@AuthenticationPrincipal Jwt jwt) {
		return service.recipients(jwt.getSubject());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DeliveryResponse create(@Valid @RequestBody DeliveryRequest request, @AuthenticationPrincipal Jwt jwt) {
		return service.create(request, jwt.getSubject());
	}

	@PostMapping("/{id}/accept")
	public DeliveryResponse accept(@PathVariable Long id, @Valid @RequestBody(required = false) DeliveryCommentRequest body,
			@AuthenticationPrincipal Jwt jwt) {
		return service.accept(id, comment(body), jwt.getSubject());
	}

	@PostMapping("/{id}/reject")
	public DeliveryResponse reject(@PathVariable Long id, @Valid @RequestBody(required = false) DeliveryCommentRequest body,
			@AuthenticationPrincipal Jwt jwt) {
		return service.reject(id, comment(body), jwt.getSubject());
	}

	@PostMapping("/{id}/cancel")
	public DeliveryResponse cancel(@PathVariable Long id, @Valid @RequestBody(required = false) DeliveryCommentRequest body,
			@AuthenticationPrincipal Jwt jwt) {
		return service.cancel(id, comment(body), jwt.getSubject());
	}

	@PostMapping("/{id}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public DeliveryResponse addComment(@PathVariable Long id, @Valid @RequestBody DeliveryCommentRequest body,
			@AuthenticationPrincipal Jwt jwt) {
		return service.comment(id, comment(body), jwt.getSubject());
	}

	private static String comment(DeliveryCommentRequest body) {
		return body == null ? null : body.comment();
	}
}
