package com.wearminutes.service.controller.delivery;

import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.dto.request.LoginRequest;
import com.wearminutes.service.dto.request.UserRequest;
import com.wearminutes.service.exception.AuthenticationException;
import com.wearminutes.service.security.JwtUtil;
import com.wearminutes.service.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery/users")
@CrossOrigin(origins = "*")
@Tag(name = "Delivery Personnel Management", description = "Endpoints for delivery personnel registration and profile management")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login delivery personnel")
    public ResponseEntity<Map<String, String>> loginDelivery(@RequestBody LoginRequest request) {
        User user = deliveryService.findByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid password");
        }
        Map<String, String> response = new HashMap<>();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("userId", user.getId().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new delivery personnel")
    public ResponseEntity<User> registerDelivery(@RequestBody UserRequest request) {
        User user = deliveryService.registerDeliveryPersonnel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Get all delivery personnel")
    public ResponseEntity<List<User>> getAllDeliveryPersonnel() {
        List<User> personnel = deliveryService.getAllDeliveryPersonnel();
        return ResponseEntity.ok(personnel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery personnel by ID")
    public ResponseEntity<User> getDeliveryPersonnelById(@PathVariable Long id) {
        User user = deliveryService.getDeliveryPersonnelById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update delivery personnel details")
    public ResponseEntity<User> updateDeliveryPersonnel(@PathVariable Long id, @RequestBody UserRequest request) {
        User updatedUser = deliveryService.updateDeliveryPersonnel(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete delivery personnel")
    public ResponseEntity<String> deleteDeliveryPersonnel(@PathVariable Long id) {
        deliveryService.deleteDeliveryPersonnel(id);
        return ResponseEntity.ok("Delivery personnel deleted successfully with ID: " + id);
    }
}
