package com.lowcode.webfrontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final WebClient webClient;

    @Value("${backend.user-service.url:http://localhost:8080/api/user}")
    private String userServiceUrl;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/do-login")
    public String login(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        try {
            String loginUrl = userServiceUrl + "/auth/login";
            Map<String, String> requestBody = Map.of("username", username, "password", password);
            System.out.println("[DEBUG] Attempting login for: " + username);
            System.out.println("[DEBUG] Login URL: " + loginUrl);
            System.out.println("[DEBUG] Request body: " + requestBody);
            Map<String, String> response = webClient.post()
                    .uri(loginUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            System.out.println("[DEBUG] Login response: " + response);
            if (response != null && response.get("token") != null) {
                session.setAttribute("jwt", response.get("token"));
                // Set authentication in Spring Security
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
                // Persist authentication in session for future requests
                SecurityContext context = SecurityContextHolder.getContext();
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception ex) {
            System.out.println("[DEBUG] Login exception: " + ex.getMessage());
            model.addAttribute("error", "Login failed: " + ex.getMessage());
            return "login";
        }
    }

    @PostMapping("/do-register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            webClient.post()
                    .uri(userServiceUrl + "/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("username", username, "password", password))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            model.addAttribute("success", "Registration successful. Please login.");
            return "login";
        } catch (Exception ex) {
            model.addAttribute("error", "Registration failed: " + ex.getMessage());
            return "register";
        }
    }
} 