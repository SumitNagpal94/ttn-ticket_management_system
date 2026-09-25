package com.tms.user.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.exception.TmsException;
import com.tms.user.model.dto.CreateUserRequest;
import com.tms.user.model.dto.UserResponse;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Email already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateRole(Long userId, com.tms.common.enums.UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "User not found"));
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAssignees() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), user.getRole());
    }
}
