package com.tresde_creation.tienda_tresde.back.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tresde_creation.tienda_tresde.back.model.Delivery;
import com.tresde_creation.tienda_tresde.back.model.DeliveryStatus;

import jakarta.persistence.LockModeType;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

	@Query("select d from Delivery d order by d.deliveredAt desc, d.id desc")
	List<Delivery> findAllNewestFirst();

	/** Entregas en las que participa el usuario (las que hizo o las que recibe). */
	@Query("""
			select d from Delivery d
			where lower(d.senderUsername) = lower(:username) or lower(d.recipientUsername) = lower(:username)
			order by d.deliveredAt desc, d.id desc""")
	List<Delivery> findInvolving(@Param("username") String username);

	long countByRecipientUsernameIgnoreCaseAndStatus(String recipientUsername, DeliveryStatus status);

	/** Bloquea la fila: evita que dos clics simultáneos en "Aceptar" sumen el stock dos veces. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from Delivery d where d.id = :id")
	Optional<Delivery> findForUpdate(@Param("id") Long id);
}
