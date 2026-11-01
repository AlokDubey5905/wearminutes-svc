package com.wearminutes.service.dto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "kafka_outbox", indexes = @Index(name = "idx_kafka_outbox_pending", columnList = "published_at, id"))
@Getter
@Setter
public class KafkaOutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(name = "deduplication_key", nullable = false, unique = true, length = 200)
    private String deduplicationKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
