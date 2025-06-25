package com.lowcode.webfrontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final WebClient webClient;

    @Value("${backend.workflow-service.url:http://localhost:8080/api/workflow}")
    private String workflowServiceUrl;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            List<Map> workflows = webClient.get()
                    .uri(workflowServiceUrl + "/workflows")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            model.addAttribute("workflows", workflows);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch workflows: " + ex.getMessage());
        }
        return "dashboard";
    }
} 