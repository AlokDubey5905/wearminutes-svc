package com.wearminutes.service.dto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "delivery_otp", length = 4)
    private String deliveryOtp;

    @Column(name = "delivery_address", length = 1000)
    private String deliveryAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMode {PREPAID, COD}

    public enum OrderStatus {PLACED, ACCEPTED, PACKED, OUT_FOR_DELIVERY, DELIVERED}
}