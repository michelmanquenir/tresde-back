package com.tresde_creation.tienda_tresde.back.auth;

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
}
