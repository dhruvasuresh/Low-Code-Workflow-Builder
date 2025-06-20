package com.lowcode.userservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private Set<String> roles;
    private LocalDateTime createdAt;
} 