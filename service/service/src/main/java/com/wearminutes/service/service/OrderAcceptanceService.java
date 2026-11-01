package com.wearminutes.service.service;

import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderAcceptanceService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderAcceptanceService(OrderRepository orderRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void acceptOrderAndQueueDeliveryTask(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found for orderId=" + orderId));
        if (order.getOrderStatus() == Order.OrderStatus.PLACED) {
            order.setOrderStatus(Order.OrderStatus.ACCEPTED);
            orderRepository.save(order);
        }
        notificationService.queueDeliveryTask(orderId);
    }
}
