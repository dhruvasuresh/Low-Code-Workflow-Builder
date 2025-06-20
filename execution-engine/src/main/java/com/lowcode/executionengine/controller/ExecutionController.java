package com.lowcode.executionengine.controller;

import com.lowcode.executionengine.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/workflow/{workflowId}")
    public ResponseEntity<Map<String, Object>> executeWorkflow(@PathVariable String workflowId, @RequestHeader("Authorization") String token) {
        executionService.executeWorkflow(workflowId, token);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Workflow execution started");
        response.put("workflowId", workflowId);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
} 