package com.b2b.inventorysystem.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private String email;
    private String fullName;
    private String roleName;

    public AuthResponse(String token, String email, String fullName, String roleName) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.roleName = roleName;
    }
}