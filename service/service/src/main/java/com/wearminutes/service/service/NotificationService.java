package com.wearminutes.service.service;

import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.dto.entity.KafkaOutboxMessage;
import com.wearminutes.service.event.DeliveryTaskEvent;
import com.wearminutes.service.event.OrderPlacedEvent;
import com.wearminutes.service.repository.KafkaOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String ORDER_TOPIC = "order-placed";
    private static final String DELIVERY_TOPIC = "delivery-tasks";

    private final KafkaOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(KafkaOutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void queueVendorNotification(Order order) {
        saveOutboxMessage(ORDER_TOPIC, "order-placed:" + order.getId(), new OrderPlacedEvent(order));
    }

    public void queueDeliveryTask(Long orderId) {
        saveOutboxMessage(DELIVERY_TOPIC, "delivery-task:" + orderId, new DeliveryTaskEvent(orderId));
    }

    private void saveOutboxMessage(String topic, String deduplicationKey, Object event) {
        if (outboxRepository.existsByDeduplicationKey(deduplicationKey)) {
            return;
        }
        try {
            KafkaOutboxMessage message = new KafkaOutboxMessage();
            message.setTopic(topic);
            message.setDeduplicationKey(deduplicationKey);
            message.setPayload(objectMapper.writeValueAsString(event));
            outboxRepository.save(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize Kafka event for " + deduplicationKey, ex);
        }
    }
}
