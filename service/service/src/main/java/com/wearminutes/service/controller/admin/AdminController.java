package com.wearminutes.service.controller.admin;

import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.dto.request.LoginRequest;
import com.wearminutes.service.dto.request.UserRequest;
import com.wearminutes.service.exception.AuthenticationException;
import com.wearminutes.service.security.JwtUtil;
import com.wearminutes.service.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@Tag(name = "Admin Management", description = "Endpoints for admin registration and platform management")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login admin")
    public ResponseEntity<Map<String, String>> loginAdmin(@RequestBody LoginRequest request) {
        User user = adminService.findByEmail(request.getEmail());
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
    @Operation(summary = "Register a new admin")
    public ResponseEntity<User> registerAdmin(@RequestBody UserRequest request) {
        User user = adminService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Get all users (all roles)")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User updatedUser = adminService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully with ID: " + id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate/Deactivate user")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        User user = adminService.toggleUserStatus(id, isActive);
        return ResponseEntity.ok(user);
    }
}
