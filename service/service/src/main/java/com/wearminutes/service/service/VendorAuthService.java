package com.wearminutes.service.service;

import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.dto.request.UserRequest;
import com.wearminutes.service.exception.AuthenticationException;
import com.wearminutes.service.exception.ResourceNotFoundException;
import com.wearminutes.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VendorAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Register new vendor
    @Transactional
    public User registerVendor(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPincode(request.getPincode());
        user.setRole("VENDOR");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        user.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(user);
    }

    // 2. Fetch all vendors
    public List<User> getAllVendors() {
        return userRepository.findAll().stream()
                .filter(user -> "VENDOR".equals(user.getRole()))
                .toList();
    }

    // 3. Fetch vendor by ID
    public User getVendorById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));
        if (!"VENDOR".equals(user.getRole())) {
            throw new IllegalArgumentException("User with ID " + id + " is not a VENDOR");
        }
        return user;
    }

    // 4. Update vendor details
    @Transactional
    public User updateVendor(Long id, UserRequest request) {
        User existingUser = getVendorById(id);

        existingUser.setFullName(request.getFullName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setAddress(request.getAddress());
        existingUser.setCity(request.getCity());
        existingUser.setState(request.getState());
        existingUser.setPincode(request.getPincode());
        existingUser.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(existingUser);
    }

    @Transactional
    public String updateVendorEmail(String currentEmail, String newEmail) {
        if (newEmail == null || newEmail.isBlank() || newEmail.length() > 100 || !newEmail.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required");
        }

        User vendor = userRepository.findByEmail(currentEmail)
                .filter(user -> "VENDOR".equals(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "email", currentEmail));
        String normalizedEmail = newEmail.trim();
        if (!vendor.getEmail().equalsIgnoreCase(normalizedEmail)
                && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered: " + normalizedEmail);
        }

        vendor.setEmail(normalizedEmail);
        vendor.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
        userRepository.save(vendor);
        return normalizedEmail;
    }

    // 5. Delete vendor
    @Transactional
    public void deleteVendor(Long id) {
        User user = getVendorById(id);
        userRepository.delete(user);
    }

    // 6. Find user by email (for authentication)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }
}
