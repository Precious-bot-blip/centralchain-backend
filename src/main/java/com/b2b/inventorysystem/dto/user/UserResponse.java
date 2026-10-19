package com.b2b.inventorysystem.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Integer userId;
    private String fullName;
    private String email;
    private Integer roleId;
    private String roleName;
    private LocalDateTime createdAt;
}