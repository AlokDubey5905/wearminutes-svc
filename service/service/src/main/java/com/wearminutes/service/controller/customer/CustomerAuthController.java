package com.wearminutes.service.controller.customer;

import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.dto.request.LoginRequest;
import com.wearminutes.service.dto.request.UserRequest;
import com.wearminutes.service.exception.AuthenticationException;
import com.wearminutes.service.security.JwtUtil;
import com.wearminutes.service.service.CustomerService;
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
@RequestMapping("/api/customer/users")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Auth Management", description = "Endpoints for customer registration and profile management")
public class CustomerAuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login customer")
    public ResponseEntity<Map<String, String>> loginCustomer(@RequestBody LoginRequest request) {
        User user = customerService.findByEmail(request.getEmail());
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
    @Operation(summary = "Register a new customer")
    public ResponseEntity<User> registerCustomer(@RequestBody UserRequest request) {
        User user = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        List<User> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<User> getCustomerById(@PathVariable Long id) {
        User user = customerService.getCustomerById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public ResponseEntity<User> updateCustomer(@PathVariable Long id, @RequestBody UserRequest request) {
        User updatedUser = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully with ID: " + id);
    }
}
