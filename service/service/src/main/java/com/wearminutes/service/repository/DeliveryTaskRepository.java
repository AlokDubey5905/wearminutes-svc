package com.wearminutes.service.repository;

import com.wearminutes.service.dto.entity.DeliveryTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {

    Optional<DeliveryTask> findByOrderId(Long orderId);

    long countByDeliveryPersonIdAndStatusIn(Long deliveryPersonId, Collection<DeliveryTask.Status> statuses);

    List<DeliveryTask> findByDeliveryPersonIdOrderByCreatedAtDesc(Long deliveryPersonId);

    List<DeliveryTask> findByStatusOrderByCreatedAtAsc(DeliveryTask.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DeliveryTask> findWithLockingById(Long id);
}
