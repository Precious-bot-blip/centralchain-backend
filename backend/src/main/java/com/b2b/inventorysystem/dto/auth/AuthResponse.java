package com.b2b.inventorysystem.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String email;
    private String fullName;
    private String roleName;
    private String otp;

    public AuthResponse(String token, String email, String fullName, String roleName) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.roleName = roleName;
    }

    public AuthResponse(String token, String email, String fullName, String roleName, String otp) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.roleName = roleName;
        this.otp = otp;
    }
}