package com.lowcode.executionengine.controller;

import com.lowcode.executionengine.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/workflow/{workflowId}")
    public ResponseEntity<String> executeWorkflow(@PathVariable String workflowId, @RequestHeader("Authorization") String token) {
        executionService.executeWorkflow(workflowId, token);
        return ResponseEntity.ok("Workflow execution started for ID: " + workflowId);
    }
} 