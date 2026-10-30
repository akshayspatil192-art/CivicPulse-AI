package com.civicpulse.service;

import com.civicpulse.dto.AuthRequest;
import com.civicpulse.dto.AuthResponse;
import com.civicpulse.dto.RegisterRequest;
import com.civicpulse.entity.Role;
import com.civicpulse.entity.User;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("An account with email " + req.getEmail() + " already exists.");
        }

        User user = new User(
            req.getFullName(),
            req.getEmail().toLowerCase().trim(),
            req.getPhone(),
            passwordEncoder.encode(req.getPassword()),
            req.getRole() != null ? req.getRole() : Role.CITIZEN
        );

        User saved = userRepository.save(user);
        String mockToken = "token_" + UUID.randomUUID().toString();

        return new AuthResponse(
            mockToken,
            saved.getId(),
            saved.getFullName(),
            saved.getEmail(),
            saved.getRole(),
            "Registration successful."
        );
    }

    public AuthResponse authenticate(AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        String mockToken = "token_" + UUID.randomUUID().toString();
        return new AuthResponse(
            mockToken,
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            "Authentication successful."
        );
    }

    public User getById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }
}
