package com.tresde_creation.tienda_tresde.back.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tresde_creation.tienda_tresde.back.model.DeliveryComment;

public interface DeliveryCommentRepository extends JpaRepository<DeliveryComment, Long> {

	List<DeliveryComment> findByDeliveryIdInOrderByCreatedAtAscIdAsc(Collection<Long> deliveryIds);
}
