package com.tresde_creation.tienda_tresde.back.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tresde_creation.tienda_tresde.back.dto.UserRequest;
import com.tresde_creation.tienda_tresde.back.dto.UserResponse;
import com.tresde_creation.tienda_tresde.back.model.User;
import com.tresde_creation.tienda_tresde.back.repository.UserRepository;
import com.tresde_creation.tienda_tresde.back.utils.ApiException;

/** Usuarios del panel: solo el administrador los crea (no hay registro público). */
@Service
public class UserService {

	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_STAFF = "STAFF";

	private static final String USERNAME_TAKEN = "Ya existe un usuario con ese nombre de usuario.";

	private final UserRepository users;
	private final PasswordEncoder encoder;

	public UserService(UserRepository users, PasswordEncoder encoder) {
		this.users = users;
		this.encoder = encoder;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list() {
		return users.findAllByOrderByCreatedAtAsc().stream().map(UserService::toResponse).toList();
	}

	@Transactional
	public UserResponse create(UserRequest request) {
		String username = request.username() == null ? "" : request.username().trim().toLowerCase();
		if (username.isEmpty()) throw fieldError("username", "Ingresa el nombre de usuario.");
		checkPassword(request.password(), true);
		if (users.existsByUsernameIgnoreCase(username)) throw fieldError("username", USERNAME_TAKEN);
		try {
			User user = users.saveAndFlush(
					new User(username, encoder.encode(request.password()), request.name().trim(), request.role()));
			return toResponse(user);
		} catch (DataIntegrityViolationException e) {
			throw fieldError("username", USERNAME_TAKEN);
		}
	}

	/** Edita nombre, rol y (opcionalmente) contraseña. currentUsername: quien hace el cambio. */
	@Transactional
	public UserResponse update(Long id, UserRequest request, String currentUsername) {
		User user = find(id);
		boolean self = user.getUsername().equalsIgnoreCase(currentUsername);
		if (self && !ROLE_ADMIN.equals(request.role())) {
			throw new ApiException(HttpStatus.CONFLICT, "No puedes quitarte el rol de administrador a ti mismo.");
		}
		user.setName(request.name().trim());
		user.setRole(request.role());
		if (request.password() != null && !request.password().isEmpty()) {
			checkPassword(request.password(), false);
			user.changePassword(encoder.encode(request.password()));
		}
		return toResponse(user);
	}

	@Transactional
	public void delete(Long id, String currentUsername) {
		User user = find(id);
		if (user.getUsername().equalsIgnoreCase(currentUsername)) {
			throw new ApiException(HttpStatus.CONFLICT, "No puedes eliminar tu propio usuario.");
		}
		users.delete(user);
	}

	private User find(Long id) {
		return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
	}

	private static void checkPassword(String password, boolean required) {
		if (password == null || password.isEmpty()) {
			if (required) throw fieldError("password", "Ingresa una contraseña.");
			return;
		}
		if (password.length() < 8) throw fieldError("password", "La contraseña debe tener al menos 8 caracteres.");
	}

	private static ApiException fieldError(String field, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message, Map.of(field, message));
	}

	private static UserResponse toResponse(User u) {
		return new UserResponse(u.getId(), u.getUsername(), u.getName(), u.getRole(), u.getCreatedAt());
	}
}
