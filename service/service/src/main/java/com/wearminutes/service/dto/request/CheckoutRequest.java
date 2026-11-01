package com.wearminutes.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private Long customerId;
    private Long productId;
    private int quantity;
    private String  paymentMode;
    private String deliveryAddress;
}