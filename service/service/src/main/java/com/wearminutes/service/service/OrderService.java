package com.wearminutes.service.service;

import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.dto.entity.Product;
import com.wearminutes.service.exception.ConcurrentModificationException;
import com.wearminutes.service.exception.OutOfStockException;
import com.wearminutes.service.exception.ResourceNotFoundException;
import com.wearminutes.service.repository.OrderRepository;
import com.wearminutes.service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Order checkoutOrder(Long customerId, Long productId, int quantity, String paymentMode, String address) {
        logger.info("[ORDER SERVICE] Start checkoutOrder customerId={}, productId={}, quantity={}, paymentMode={}, address={}",
                customerId, productId, quantity, paymentMode, address);
        try {
            logger.info("[ORDER SERVICE] Fetching product by id={}", productId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
            logger.info("[ORDER SERVICE] Product fetched: title={}, stock={}", product.getTitle(), product.getStockQuantity());

            if (product.getStockQuantity() < quantity) {
                logger.error("[ORDER SERVICE] Out of stock: title={}, available={}, requested={}",
                        product.getTitle(), product.getStockQuantity(), quantity);
                throw new OutOfStockException(product.getTitle(), product.getStockQuantity(), quantity);
            }

            product.setStockQuantity(product.getStockQuantity() - quantity);
            logger.info("[ORDER SERVICE] Saving updated product stock={}", product.getStockQuantity());
            productRepository.save(product);
            logger.info("[ORDER SERVICE] Product saved successfully");

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setShopId(product.getShop().getId());
            order.setTotalAmount(product.getPrice() * quantity);
            order.setPaymentMode(Order.PaymentMode.valueOf(paymentMode));
            order.setOrderStatus(Order.OrderStatus.PLACED);
            order.setDeliveryOtp(generateRandomOtp());
            order.setDeliveryAddress(address);

            logger.info("[ORDER SERVICE] Saving new order for customerId={}, shopId={}, totalAmount={}",
                    customerId, product.getShop().getId(), order.getTotalAmount());
            Order savedOrder = orderRepository.save(order);
            logger.info("[ORDER SERVICE] Order saved with id={}", savedOrder.getId());

            logger.info("[ORDER SERVICE] Notifying vendor for orderId={}", savedOrder.getId());
            notificationService.queueVendorNotification(savedOrder);
            logger.info("[ORDER SERVICE] Vendor notified for orderId={}", savedOrder.getId());

            logger.info("[ORDER SERVICE] End checkoutOrder orderId={}", savedOrder.getId());
            return savedOrder;

        } catch (ObjectOptimisticLockingFailureException ex) {
            logger.error("[ORDER SERVICE] Concurrent modification during checkout: {}", ex.getMessage(), ex);
            throw new ConcurrentModificationException("Product", "checkout");
        }
    }

    private String generateRandomOtp() {
        int otp = 1000 + (int) (Math.random() * 9000);
        String otpStr = String.valueOf(otp);
        logger.info("[ORDER SERVICE] Generated OTP={}", otpStr);
        return otpStr;
    }
}
