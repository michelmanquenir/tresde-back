package com.tresde_creation.tienda_tresde.back.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.DeliveryRequest;
import com.tresde_creation.tienda_tresde.back.dto.DeliveryResponse;
import com.tresde_creation.tienda_tresde.back.model.Delivery;
import com.tresde_creation.tienda_tresde.back.model.DeliveryComment;
import com.tresde_creation.tienda_tresde.back.model.DeliveryItem;
import com.tresde_creation.tienda_tresde.back.model.DeliveryStatus;
import com.tresde_creation.tienda_tresde.back.model.Product;
import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.DeliveryCommentRepository;
import com.tresde_creation.tienda_tresde.back.repository.DeliveryRepository;
import com.tresde_creation.tienda_tresde.back.repository.ProductRepository;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/**
 * Entregas de inventario entre usuarios del panel. Quien entrega registra qué productos y cuántas
 * unidades; el stock recién se suma cuando quien recibe la acepta.
 *
 * Permisos: quien recibe acepta o rechaza; quien entrega (o un administrador) la cancela mientras
 * esté pendiente. Comentan los participantes y los administradores. Un trabajador solo ve las
 * entregas en las que participa; el administrador ve todas.
 */
@Service
public class DeliveryService {

	private final DeliveryRepository deliveries;
	private final DeliveryCommentRepository comments;
	private final ProductRepository products;
	private final UserRepository users;

	public DeliveryService(DeliveryRepository deliveries, DeliveryCommentRepository comments,
			ProductRepository products, UserRepository users) {
		this.deliveries = deliveries;
		this.comments = comments;
		this.products = products;
		this.users = users;
	}

	/** status: PENDING, ACCEPTED… (opcional). */
	@Transactional(readOnly = true)
	public List<DeliveryResponse> list(String username, String status) {
		User me = currentUser(username);
		DeliveryStatus filter = parseStatus(status);
		List<Delivery> list = isAdmin(me) ? deliveries.findAllNewestFirst() : deliveries.findInvolving(me.getUsername());
		if (filter != null) list = list.stream().filter(d -> d.getStatus() == filter).toList();
		return toResponses(list);
	}

	@Transactional(readOnly = true)
	public DeliveryResponse get(Long id, String username) {
		User me = currentUser(username);
		Delivery d = find(id);
		checkCanSee(d, me);
		return toResponse(d);
	}

	/** Cuántas entregas esperan que yo las acepte (para el aviso del menú). */
	@Transactional(readOnly = true)
	public long pendingForMe(String username) {
		return deliveries.countByRecipientUsernameIgnoreCaseAndStatus(username, DeliveryStatus.PENDING);
	}

	/** Usuarios del panel a los que puedo entregar (todos menos yo). */
	@Transactional(readOnly = true)
	public List<DeliveryResponse.Recipient> recipients(String username) {
		return users.findAllByOrderByCreatedAtAsc().stream()
				.filter(DeliveryService::isPanelUser)
				.filter(u -> !u.getUsername().equalsIgnoreCase(username))
				.map(u -> new DeliveryResponse.Recipient(u.getUsername(), u.getName(), u.getRole()))
				.toList();
	}

	@Transactional
	public DeliveryResponse create(DeliveryRequest req, String username) {
		User sender = currentUser(username);
		User recipient = users.findByUsernameIgnoreCase(req.recipientUsername().trim())
				.filter(DeliveryService::isPanelUser)
				.orElseThrow(() -> fieldError("recipientUsername", "Ese usuario no existe."));
		if (recipient.getId().equals(sender.getId())) {
			throw fieldError("recipientUsername", "No puedes entregarte inventario a ti mismo.");
		}
		if (req.deliveredAt() != null && req.deliveredAt().isAfter(Instant.now().plusSeconds(24 * 3600))) {
			throw fieldError("deliveredAt", "La fecha de entrega no puede ser futura.");
		}

		// Si el mismo producto viene dos veces, se suman las cantidades
		Map<String, Integer> quantities = new LinkedHashMap<>();
		for (DeliveryRequest.Item item : req.items()) {
			quantities.merge(item.productId().trim(), item.quantity(), Integer::sum);
		}

		Delivery d = new Delivery();
		d.setSenderUsername(sender.getUsername());
		d.setSenderName(sender.getName());
		d.setRecipientUsername(recipient.getUsername());
		d.setRecipientName(recipient.getName());
		d.setNote(blankToNull(req.note()));
		d.setDeliveredAt(req.deliveredAt());
		quantities.forEach((productId, qty) -> {
			Product p = products.findById(productId)
					.orElseThrow(() -> ApiException.notFound("Producto no encontrado: " + productId + "."));
			if (qty > 10000) throw ApiException.badRequest("Demasiadas unidades de \"" + p.getName() + "\".");
			d.getItems().add(new DeliveryItem(p.getId(), p.getName(), qty));
		});
		return toResponse(deliveries.save(d));
	}

	/** Quien recibe la acepta: recién aquí se suma el stock de cada producto. */
	@Transactional
	public DeliveryResponse accept(Long id, String comment, String username) {
		User me = currentUser(username);
		Delivery d = findPendingForUpdate(id);
		if (!d.getRecipientUsername().equalsIgnoreCase(me.getUsername())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Solo " + d.getRecipientName() + " puede aceptar esta entrega.");
		}
		for (DeliveryItem item : d.getItems()) {
			if (item.getProductId() == null) continue; // el producto se eliminó: no hay stock que sumar
			products.findById(item.getProductId()).ifPresent(p -> p.setStock(p.getStock() + item.getQuantity()));
		}
		d.resolve(DeliveryStatus.ACCEPTED, me.getUsername());
		addComment(d, me, comment);
		return toResponse(d);
	}

	/** Quien recibe la rechaza (no se suma nada). */
	@Transactional
	public DeliveryResponse reject(Long id, String comment, String username) {
		User me = currentUser(username);
		Delivery d = findPendingForUpdate(id);
		if (!d.getRecipientUsername().equalsIgnoreCase(me.getUsername())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Solo " + d.getRecipientName() + " puede rechazar esta entrega.");
		}
		d.resolve(DeliveryStatus.REJECTED, me.getUsername());
		addComment(d, me, comment);
		return toResponse(d);
	}

	/** Quien entregó (o un administrador) la anula antes de que la acepten. */
	@Transactional
	public DeliveryResponse cancel(Long id, String comment, String username) {
		User me = currentUser(username);
		Delivery d = findPendingForUpdate(id);
		if (!d.getSenderUsername().equalsIgnoreCase(me.getUsername()) && !isAdmin(me)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Solo " + d.getSenderName() + " puede cancelar esta entrega.");
		}
		d.resolve(DeliveryStatus.CANCELLED, me.getUsername());
		addComment(d, me, comment);
		return toResponse(d);
	}

	@Transactional
	public DeliveryResponse comment(Long id, String comment, String username) {
		User me = currentUser(username);
		Delivery d = find(id);
		checkCanSee(d, me);
		if (blankToNull(comment) == null) throw fieldError("comment", "Escribe un comentario.");
		addComment(d, me, comment);
		return toResponse(d);
	}

	// ── Ayudantes ──

	private void addComment(Delivery d, User author, String body) {
		String text = blankToNull(body);
		if (text != null) comments.save(new DeliveryComment(d.getId(), author, text));
	}

	private Delivery find(Long id) {
		return deliveries.findById(id).orElseThrow(() -> ApiException.notFound("Entrega no encontrada."));
	}

	private Delivery findPendingForUpdate(Long id) {
		Delivery d = deliveries.findForUpdate(id).orElseThrow(() -> ApiException.notFound("Entrega no encontrada."));
		if (d.getStatus() != DeliveryStatus.PENDING) {
			throw new ApiException(HttpStatus.CONFLICT, switch (d.getStatus()) {
				case ACCEPTED -> "Esta entrega ya fue aceptada.";
				case REJECTED -> "Esta entrega ya fue rechazada.";
				default -> "Esta entrega fue cancelada.";
			});
		}
		return d;
	}

	private void checkCanSee(Delivery d, User me) {
		if (!isAdmin(me) && !d.involves(me.getUsername())) {
			throw ApiException.notFound("Entrega no encontrada.");
		}
	}

	private User currentUser(String username) {
		return users.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tu usuario ya no existe."));
	}

	private static boolean isAdmin(User u) {
		return UserService.ROLE_ADMIN.equals(u.getRole());
	}

	private static boolean isPanelUser(User u) {
		return UserService.ROLE_ADMIN.equals(u.getRole()) || UserService.ROLE_STAFF.equals(u.getRole());
	}

	private static DeliveryStatus parseStatus(String status) {
		if (status == null || status.isBlank()) return null;
		try {
			return DeliveryStatus.valueOf(status.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw ApiException.badRequest("Estado inválido: " + status + ".");
		}
	}

	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v.trim();
	}

	private static ApiException fieldError(String field, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message, Map.of(field, message));
	}

	private DeliveryResponse toResponse(Delivery d) {
		return toResponses(List.of(d)).get(0);
	}

	/** Trae los comentarios de todas las entregas en una sola consulta. */
	private List<DeliveryResponse> toResponses(List<Delivery> list) {
		if (list.isEmpty()) return List.of();
		Map<Long, List<DeliveryResponse.Comment>> byDelivery = comments
				.findByDeliveryIdInOrderByCreatedAtAscIdAsc(list.stream().map(Delivery::getId).toList())
				.stream()
				.collect(Collectors.groupingBy(DeliveryComment::getDeliveryId, LinkedHashMap::new,
						Collectors.mapping(c -> new DeliveryResponse.Comment(c.getId(), c.getAuthorUsername(),
								c.getAuthorName(), c.getBody(), c.getCreatedAt()), Collectors.toList())));
		List<DeliveryResponse> out = new ArrayList<>(list.size());
		for (Delivery d : list) {
			out.add(new DeliveryResponse(d.getId(), d.getSenderUsername(), d.getSenderName(),
					d.getRecipientUsername(), d.getRecipientName(), d.getStatus().name(), d.getNote(), d.totalUnits(),
					d.getItems().stream()
							.map(i -> new DeliveryResponse.Item(i.getProductId(), i.getProductName(), i.getQuantity()))
							.toList(),
					byDelivery.getOrDefault(d.getId(), List.of()),
					d.getDeliveredAt(), d.getResolvedAt(), d.getResolvedBy(), d.getCreatedAt()));
		}
		return out;
	}
}
