package com.wearminutes.service.dto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tasks", uniqueConstraints = @UniqueConstraint(
        name = "uk_delivery_tasks_order_id",
        columnNames = "order_id"
))
@Getter
@Setter
public class DeliveryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "delivery_person_id")
    private Long deliveryPersonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED
    }
}
