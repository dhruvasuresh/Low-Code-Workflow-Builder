package com.lowcode.workflowservice.controller;

import com.lowcode.workflowservice.dto.WorkflowStepDto;
import com.lowcode.workflowservice.service.WorkflowStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/workflows/{workflowId}/steps")
@RequiredArgsConstructor
public class WorkflowStepController {
    private final WorkflowStepService stepService;

    @PostMapping
    public ResponseEntity<WorkflowStepDto> addStep(@PathVariable Long workflowId, @RequestBody WorkflowStepDto dto) {
        WorkflowStepDto created = stepService.addStep(workflowId, dto);
        return ResponseEntity.created(URI.create("/workflows/" + workflowId + "/steps/" + created.getId())).body(created);
    }

    @PutMapping("/{stepId}")
    public ResponseEntity<WorkflowStepDto> updateStep(@PathVariable Long workflowId, @PathVariable Long stepId, @RequestBody WorkflowStepDto dto) {
        WorkflowStepDto updated = stepService.updateStep(workflowId, stepId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable Long workflowId, @PathVariable Long stepId) {
        stepService.deleteStep(workflowId, stepId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WorkflowStepDto>> listSteps(@PathVariable Long workflowId) {
        return ResponseEntity.ok(stepService.listSteps(workflowId));
    }
} 