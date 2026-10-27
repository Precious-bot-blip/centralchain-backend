package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.auth.AuthResponse;
import com.b2b.inventorysystem.dto.auth.LoginRequest;
import com.b2b.inventorysystem.dto.auth.RegisterRequest;
import com.b2b.inventorysystem.dto.auth.VerifyRequest;
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

    // REGISTER (OTP)
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationCode(otp);
        user.setIsVerified(false);

        User saved = userRepository.save(user);

        System.out.println("=========================================");
        System.out.println("OTP FOR " + saved.getEmail() + " IS: " + otp);
        System.out.println("=========================================");

        return new AuthResponse("PENDING_VERIFICATION", saved.getEmail(), saved.getFullName(), role.getRoleName(), otp);
    }

    // VERIFY ACCOUNT
    public String verifyAccount(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getIsVerified()) {
            throw new BadRequestException("User is already verified");
        }

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(request.getVerificationCode())) {
            user.setIsVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return "Account verified successfully! You can now log in.";
        } else {
            throw new BadRequestException("Invalid verification code");
        }
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getIsVerified()) {
            throw new BadRequestException("Please verify your email first. Code: " + user.getVerificationCode());
        }

        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRole().getRoleName());

        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().getRoleName());
    }
}