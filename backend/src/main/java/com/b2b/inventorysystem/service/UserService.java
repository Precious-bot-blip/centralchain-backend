package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.user.UserCreateRequest;
import com.b2b.inventorysystem.dto.user.UserResponse;
import com.b2b.inventorysystem.dto.user.UserUpdateRequest;
import com.b2b.inventorysystem.entity.Role;
import com.b2b.inventorysystem.entity.User;
import com.b2b.inventorysystem.exception.BadRequestException;
import com.b2b.inventorysystem.exception.ConflictException;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.RoleRepository;
import com.b2b.inventorysystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // READ
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    public List<UserResponse> getUsersByRole(Integer roleId) {
        return userRepository.findByRoleRoleId(roleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    public long countUsers() {
        return userRepository.count();
    }

    // CREATE
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + request.getRoleName()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    // UPDATE
    public UserResponse updateUser(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + request.getRoleName()));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(role);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    // DELETE
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        try {
            userRepository.delete(user);
        } catch (Exception ex) {
            throw new ConflictException(
                    "Cannot delete user — they have references (orders, deliveries, messages)");
        }
    }

    // HELPER
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getRoleId(),
                user.getRole().getRoleName(),
                user.getCreatedAt()
        );
    }
}