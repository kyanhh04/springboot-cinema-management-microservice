package com.example.user_service.service;

import com.example.common.security.JwtUtil;
import com.example.user_service.dto.*;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Registration failed: Username already exists - {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Registration failed: Email already exists - {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setRole(User.Role.USER);
            user.setActive(true);

            user = userRepository.save(user);
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());

            return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            log.error("Error during user registration for username: {}", request.getUsername(), e);
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("Login failed: User not found - {}", request.getUsername());
                    return new RuntimeException("Invalid username or password");
                });

        if (!user.getActive()) {
            log.error("Login failed: Account is disabled - {}", request.getUsername());
            throw new RuntimeException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Login failed: Invalid password for user - {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        try {
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
            return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            log.error("Error generating token for user: {}", request.getUsername(), e);
            throw e;
        }
    }

    public UserDTO getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", id);
                        return new RuntimeException("User not found");
                    });
            return convertToDTO(user);
        } catch (Exception e) {
            log.error("Error fetching user by ID: {}", id, e);
            throw e;
        }
    }

    public UserDTO getUserByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found with username: {}", username);
                        return new RuntimeException("User not found");
                    });
            return convertToDTO(user);
        } catch (Exception e) {
            log.error("Error fetching user by username: {}", username, e);
            throw e;
        }
    }

    public List<UserDTO> getAllUsers() {
        try {
            return userRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UpdateUserRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new RuntimeException("User not found");
                    });

            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }
            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    log.error("Profile update failed: Email already exists - {}", request.getEmail());
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(request.getEmail());
            }

            user = userRepository.save(user);
            return convertToDTO(user);
        } catch (Exception e) {
            log.error("Error updating profile for user ID: {}", userId, e);
            throw e;
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new RuntimeException("User not found");
                    });

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.error("Change password failed: Current password is incorrect for user ID: {}", userId);
                throw new RuntimeException("Current password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error changing password for user ID: {}", userId, e);
            throw e;
        }
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new RuntimeException("User not found");
                    });
            user.setActive(false);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error deactivating account for user ID: {}", userId, e);
            throw e;
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().name());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
