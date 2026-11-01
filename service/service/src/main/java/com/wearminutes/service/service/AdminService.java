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
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Register new admin
    @Transactional
    public User registerAdmin(UserRequest request) {
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
        user.setRole("ADMIN");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        user.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(user);
    }

    // 2. Fetch all users (all roles)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. Fetch user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // 4. Update user details
    @Transactional
    public User updateUser(Long id, UserRequest request) {
        User existingUser = getUserById(id);

        existingUser.setFullName(request.getFullName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setAddress(request.getAddress());
        existingUser.setCity(request.getCity());
        existingUser.setState(request.getState());
        existingUser.setPincode(request.getPincode());
        existingUser.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return userRepository.save(existingUser);
    }

    // 5. Delete user
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    // 6. Activate/Deactivate user
    @Transactional
    public User toggleUserStatus(Long id, Boolean isActive) {
        User user = getUserById(id);
        user.setIsActive(isActive);
        user.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
        return userRepository.save(user);
    }

    // 7. Find user by email (for authentication)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + email));
    }
}
