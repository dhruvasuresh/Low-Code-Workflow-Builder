package com.lowcode.actionrunner.controller;

import com.lowcode.actionrunner.service.ActionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeAction(@RequestBody ActionRequest request) {
        log.info("Executing action: type={}, config={}", request.getActionType(), request.getConfig());
        String result = actionService.executeAction(request.getActionType(), request.getConfig());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("actionType", request.getActionType());
        response.put("result", result);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Data
    public static class ActionRequest {
        private String actionType;
        private Object config;
    }
} 