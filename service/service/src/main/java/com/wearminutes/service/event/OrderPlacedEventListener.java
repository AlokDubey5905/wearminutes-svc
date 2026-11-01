package com.wearminutes.service.event;

import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.dto.entity.Shop;
import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.repository.OrderRepository;
import com.wearminutes.service.repository.ShopRepository;
import com.wearminutes.service.repository.UserRepository;
import com.wearminutes.service.service.OrderAcceptanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedEventListener.class);

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final OrderAcceptanceService orderAcceptanceService;

    public OrderPlacedEventListener(
            OrderRepository orderRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            JavaMailSender mailSender,
            OrderAcceptanceService orderAcceptanceService
    ) {
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.orderAcceptanceService = orderAcceptanceService;
    }

    @KafkaListener(topics = "order-placed", groupId = "vendor-notify")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        if (event == null || event.getOrder() == null || event.getOrder().getId() == null) {
            throw new IllegalArgumentException("Order placed event must include an order ID");
        }
        Long orderId = event.getOrder().getId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found for orderId=" + orderId));

        if (order.getOrderStatus() == Order.OrderStatus.PLACED) {
            Shop shop = shopRepository.findById(order.getShopId())
                    .orElseThrow(() -> new IllegalStateException("Shop not found for shopId=" + order.getShopId()));
            User vendor = userRepository.findById(shop.getVendorId())
                    .filter(user -> "VENDOR".equals(user.getRole()))
                    .orElseThrow(() -> new IllegalStateException("Vendor not found for vendorId=" + shop.getVendorId()));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(vendor.getEmail());
            message.setSubject("New Order Placed - Order #" + order.getId());
            message.setText("Dear " + vendor.getFullName() + ",\n\n"
                    + "A new order has been placed for your shop '" + shop.getShopName() + "'.\n"
                    + "Order ID: " + order.getId() + "\n"
                    + "Total Amount: " + order.getTotalAmount() + "\n"
                    + "Delivery Address: " + order.getDeliveryAddress() + "\n\n"
                    + "Please prepare the order for delivery.\n\n"
                    + "- WearMinutes Team");
            mailSender.send(message);
            logger.info("Vendor notification email sent orderId={} vendorId={}", orderId, vendor.getId());
        } else {
            logger.info("Skipping duplicate vendor email for orderId={} status={}", orderId, order.getOrderStatus());
        }

        orderAcceptanceService.acceptOrderAndQueueDeliveryTask(orderId);
    }
}
