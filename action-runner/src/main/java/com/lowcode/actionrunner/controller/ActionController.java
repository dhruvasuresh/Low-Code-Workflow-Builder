package com.lowcode.actionrunner.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actions")
@Slf4j
public class ActionController {

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeAction(@RequestBody ActionRequest request) {
        log.info("Executing action: type={}, config={}", request.getActionType(), request.getConfig());
        // Simulate action execution
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("actionType", request.getActionType());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Data
    public static class ActionRequest {
        private String actionType;
        private Object config;
    }
} 