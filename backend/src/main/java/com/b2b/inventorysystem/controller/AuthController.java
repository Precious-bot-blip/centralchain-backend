package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.dto.auth.AuthResponse;
import com.b2b.inventorysystem.dto.auth.LoginRequest;
import com.b2b.inventorysystem.dto.auth.RegisterRequest;
import com.b2b.inventorysystem.dto.auth.VerifyRequest;
import com.b2b.inventorysystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verifyAccount(request));
    }

}