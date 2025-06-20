package com.lowcode.userservice.controller;

import com.lowcode.userservice.dto.UserRegisterRequest;
import com.lowcode.userservice.dto.UserLoginRequest;
import com.lowcode.userservice.dto.UserResponse;
import com.lowcode.userservice.domain.User;
import com.lowcode.userservice.security.JwtUtil;
import com.lowcode.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginRequest request) {
        User user = userService.authenticateUser(request);
        String token = jwtUtil.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
} 