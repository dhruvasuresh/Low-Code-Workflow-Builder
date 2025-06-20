package com.lowcode.userservice.controller;

import com.lowcode.userservice.dto.UserRegisterRequest;
import com.lowcode.userservice.dto.UserLoginRequest;
import com.lowcode.userservice.dto.UserResponse;
import com.lowcode.userservice.domain.User;
import com.lowcode.userservice.security.JwtUtil;
import com.lowcode.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginRequest request) {
        User user = userService.authenticateUser(request);
        String token = jwtUtil.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userService.getAllUsers(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("content", userPage.getContent());
        response.put("page", userPage.getNumber());
        response.put("size", userPage.getSize());
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        return ResponseEntity.ok(response);
    }
} 