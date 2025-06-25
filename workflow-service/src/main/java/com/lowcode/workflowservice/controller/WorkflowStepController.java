package com.lowcode.workflowservice.controller;

import com.lowcode.workflowservice.dto.WorkflowStepDto;
import com.lowcode.workflowservice.service.WorkflowStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.lowcode.workflowservice.domain.StepExecution;
import com.lowcode.workflowservice.repository.StepExecutionRepository;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/workflows/{workflowId}/steps")
@RequiredArgsConstructor
public class WorkflowStepController {
    private final WorkflowStepService stepService;
    private final StepExecutionRepository stepExecutionRepository;

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

    @PostMapping("/executions")
    public ResponseEntity<StepExecution> createStepExecution(@RequestParam Long workflowExecutionId, @RequestParam Long stepId, @RequestParam String status) {
        StepExecution created = stepService.createStepExecution(workflowExecutionId, stepId, status);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/executions/{stepExecutionId}")
    public ResponseEntity<StepExecution> updateStepExecutionStatus(@PathVariable Long stepExecutionId, @RequestParam String status, @RequestParam(required = false) String errorLog) {
        StepExecution updated = stepService.updateStepExecutionStatus(stepExecutionId, status, errorLog);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/executions/by-execution/{workflowExecutionId}")
    public ResponseEntity<List<StepExecution>> getStepExecutionsForWorkflowExecution(@PathVariable Long workflowExecutionId) {
        List<StepExecution> steps = stepExecutionRepository.findAll()
            .stream()
            .filter(se -> se.getWorkflowExecution().getId().equals(workflowExecutionId))
            .toList();
        return ResponseEntity.ok(steps);
    }
} 