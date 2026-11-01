package com.wearminutes.service.controller.admin;

import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.dto.request.CheckoutRequest;
import com.wearminutes.service.repository.OrderRepository;
import com.wearminutes.service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Admin Order Management", description = "Admin endpoints for managing orders")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setOrderStatus(Order.OrderStatus.valueOf(status));
        return ResponseEntity.ok(orderRepository.save(order));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Place an order (checkout)")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request) {
        Order order = orderService.checkoutOrder(
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity(),
                request.getPaymentMode(),
                request.getDeliveryAddress()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.ok("Order deleted successfully with ID: " + id);
    }
}
