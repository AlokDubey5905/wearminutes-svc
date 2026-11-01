package com.wearminutes.service.controller.vendor;

import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.dto.request.EmailUpdateRequest;
import com.wearminutes.service.dto.request.LoginRequest;
import com.wearminutes.service.dto.request.UserRequest;
import com.wearminutes.service.exception.AuthenticationException;
import com.wearminutes.service.security.JwtUtil;
import com.wearminutes.service.service.VendorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor/users")
@CrossOrigin(origins = "*")
@Tag(name = "Vendor Authentication Management", description = "Endpoints for vendor registration and profile management")
public class VendorAuthController {

    @Autowired
    private VendorAuthService vendorAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login vendor")
    public ResponseEntity<Map<String, String>> loginVendor(@RequestBody LoginRequest request) {
        User user = vendorAuthService.findByEmail(request.getEmail());
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
    @Operation(summary = "Register a new vendor")
    public ResponseEntity<User> registerVendor(@RequestBody UserRequest request) {
        User user = vendorAuthService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Get all vendors")
    public ResponseEntity<List<User>> getAllVendors() {
        List<User> vendors = vendorAuthService.getAllVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<User> getVendorById(@PathVariable Long id) {
        User user = vendorAuthService.getVendorById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vendor details")
    public ResponseEntity<User> updateVendor(@PathVariable Long id, @RequestBody UserRequest request) {
        User updatedUser = vendorAuthService.updateVendor(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/email")
    @Operation(summary = "Update the authenticated vendor's email address")
    public ResponseEntity<Map<String, String>> updateVendorEmail(
            @RequestBody EmailUpdateRequest request,
            Authentication authentication
    ) {
        String updatedEmail = vendorAuthService.updateVendorEmail(authentication.getName(), request.getEmail());
        return ResponseEntity.ok(Map.of("email", updatedEmail));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vendor")
    public ResponseEntity<String> deleteVendor(@PathVariable Long id) {
        vendorAuthService.deleteVendor(id);
        return ResponseEntity.ok("Vendor deleted successfully with ID: " + id);
    }
}
