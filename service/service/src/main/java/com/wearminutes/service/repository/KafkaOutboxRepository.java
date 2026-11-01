package com.wearminutes.service.repository;

import com.wearminutes.service.dto.entity.KafkaOutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KafkaOutboxRepository extends JpaRepository<KafkaOutboxMessage, Long> {

    boolean existsByDeduplicationKey(String deduplicationKey);

    List<KafkaOutboxMessage> findTop100ByPublishedAtIsNullOrderByIdAsc();
}
