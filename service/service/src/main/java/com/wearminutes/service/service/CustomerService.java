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
public class CustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Register new customer
    @Transactional
    public User registerCustomer(UserRequest request) {
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
        user.setRole("CUSTOMER");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        user.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(user);
    }

    // 2. Fetch all customers
    public List<User> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(user -> "CUSTOMER".equals(user.getRole()))
                .toList();
    }

    // 3. Fetch customer by ID
    public User getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (!"CUSTOMER".equals(user.getRole())) {
            throw new IllegalArgumentException("User with ID " + id + " is not a CUSTOMER");
        }
        return user;
    }

    // 4. Update customer details
    @Transactional
    public User updateCustomer(Long id, UserRequest request) {
        User existingUser = getCustomerById(id);

        existingUser.setFullName(request.getFullName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setAddress(request.getAddress());
        existingUser.setCity(request.getCity());
        existingUser.setState(request.getState());
        existingUser.setPincode(request.getPincode());
        existingUser.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(existingUser);
    }

    // 5. Delete customer
    @Transactional
    public void deleteCustomer(Long id) {
        User user = getCustomerById(id);
        userRepository.delete(user);
    }

    // 6. Find user by email (for authentication)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }
}
