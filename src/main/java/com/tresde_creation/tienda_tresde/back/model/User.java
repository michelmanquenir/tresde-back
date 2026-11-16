package com.tresde_creation.tienda_tresde.back.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	@Column(name = "password_hash")
	private String passwordHash;

	private String name;

	private String role;

	@Column(name = "created_at")
	private Instant createdAt;

	/** Los tokens emitidos antes de este momento ya no valen (se fija al cambiar la contraseña). */
	@Column(name = "sessions_since")
	private Instant sessionsSince;

	protected User() {}

	public User(String username, String passwordHash, String name, String role) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.name = name;
		this.role = role;
		this.createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getUsername() { return username; }
	public String getPasswordHash() { return passwordHash; }
	public String getName() { return name; }
	public String getRole() { return role; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getSessionsSince() { return sessionsSince; }

	public void setName(String name) { this.name = name; }
	public void setRole(String role) { this.role = role; }

	/** Cambia la contraseña y cierra las sesiones abiertas con la anterior. */
	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
		this.sessionsSince = Instant.now();
	}
}
