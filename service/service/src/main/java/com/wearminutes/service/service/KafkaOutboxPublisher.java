package com.wearminutes.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wearminutes.service.dto.entity.KafkaOutboxMessage;
import com.wearminutes.service.event.DeliveryTaskEvent;
import com.wearminutes.service.event.OrderPlacedEvent;
import com.wearminutes.service.repository.KafkaOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaOutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOutboxPublisher.class);
    private static final String ORDER_TOPIC = "order-placed";
    private static final String DELIVERY_TOPIC = "delivery-tasks";

    private final KafkaOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaOutboxPublisher(
            KafkaOutboxRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.kafka.outbox-poll-interval:1000}")
    public void publishPendingMessages() {
        List<KafkaOutboxMessage> pendingMessages = outboxRepository.findTop100ByPublishedAtIsNullOrderByIdAsc();
        for (KafkaOutboxMessage message : pendingMessages) {
            try {
                Object event = deserialize(message);
                kafkaTemplate.send(message.getTopic(), message.getDeduplicationKey(), event)
                        .get(30, TimeUnit.SECONDS);
                message.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(message);
                logger.info("Published Kafka outbox message id={} topic={}", message.getId(), message.getTopic());
            } catch (Exception ex) {
                logger.error("Failed to publish Kafka outbox message id={} topic={}",
                        message.getId(), message.getTopic(), ex);
            }
        }
    }

    private Object deserialize(KafkaOutboxMessage message) throws Exception {
        if (ORDER_TOPIC.equals(message.getTopic())) {
            return objectMapper.readValue(message.getPayload(), OrderPlacedEvent.class);
        }
        if (DELIVERY_TOPIC.equals(message.getTopic())) {
            return objectMapper.readValue(message.getPayload(), DeliveryTaskEvent.class);
        }
        throw new IllegalArgumentException("Unsupported Kafka outbox topic: " + message.getTopic());
    }
}
