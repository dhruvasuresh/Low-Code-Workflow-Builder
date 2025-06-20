package com.lowcode.actionrunner.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actions")
@Slf4j
public class ActionController {

    @PostMapping("/execute")
    public ResponseEntity<String> executeAction(@RequestBody ActionRequest request) {
        log.info("Executing action: type={}, config={}", request.getActionType(), request.getConfig());
        // Simulate action execution
        return ResponseEntity.ok("Action executed: " + request.getActionType());
    }

    @Data
    public static class ActionRequest {
        private String actionType;
        private Object config;
    }
} 