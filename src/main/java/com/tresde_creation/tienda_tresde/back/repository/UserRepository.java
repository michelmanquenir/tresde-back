package com.tresde_creation.tienda_tresde.back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tresde_creation.tienda_tresde.back.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);
}
