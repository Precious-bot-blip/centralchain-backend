package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.auth.AuthResponse;
import com.b2b.inventorysystem.dto.auth.LoginRequest;
import com.b2b.inventorysystem.dto.auth.RegisterRequest;
import com.b2b.inventorysystem.entity.Role;
import com.b2b.inventorysystem.entity.User;
import com.b2b.inventorysystem.exception.BadRequestException;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.RoleRepository;
import com.b2b.inventorysystem.repository.UserRepository;
import com.b2b.inventorysystem.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER
    public AuthResponse register(RegisterRequest request) {
        // 1. Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // 2. Look up the role
        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + request.getRoleName()));

        // 3. Build user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        // 4. Save
        User saved = userRepository.save(user);

        // 5. Generate token
        String token = jwtService.generateToken(saved.getUserId(), saved.getEmail(), role.getRoleName());

        return new AuthResponse(token, saved.getEmail(),
                saved.getFullName(), role.getRoleName());
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {
        // 1. Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        // 3. Generate token
        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRole().getRoleName());

        return new AuthResponse(token, user.getEmail(),
                user.getFullName(), user.getRole().getRoleName());
    }
}