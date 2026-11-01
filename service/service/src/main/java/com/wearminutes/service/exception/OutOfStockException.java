package com.wearminutes.service.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(String productName, int availableQuantity, int requestedQuantity) {
        super(String.format("Insufficient stock for '%s'. Available: %d, Requested: %d", productName, availableQuantity, requestedQuantity));
    }
}