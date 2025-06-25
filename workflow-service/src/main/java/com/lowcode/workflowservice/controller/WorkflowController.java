package com.lowcode.workflowservice.controller;

import com.lowcode.workflowservice.dto.WorkflowDto;
import com.lowcode.workflowservice.dto.WorkflowExecutionDto;
import com.lowcode.workflowservice.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowDto dto) {
        WorkflowDto created = workflowService.createWorkflow(dto);
        return ResponseEntity.created(URI.create("/workflows/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<WorkflowDto>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDto> getWorkflowById(@PathVariable Long id) {
        return workflowService.getWorkflowById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable Long id, @RequestBody WorkflowDto dto) {
        return workflowService.updateWorkflow(id, dto)
                .map(updated -> ResponseEntity.ok().body(updated))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/executions")
    public ResponseEntity<WorkflowExecutionDto> createWorkflowExecution(@PathVariable Long id) {
        WorkflowExecutionDto created = workflowService.createWorkflowExecution(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/workflows/" + id + "/executions/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<List<WorkflowExecutionDto>> getExecutionsForWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getExecutionsForWorkflow(id));
    }

    @PutMapping("/executions/{executionId}/status")
    public ResponseEntity<WorkflowExecutionDto> updateWorkflowExecutionStatus(@PathVariable Long executionId, @RequestParam String status) {
        WorkflowExecutionDto updated = workflowService.updateWorkflowExecutionStatus(executionId, status);
        return ResponseEntity.ok(updated);
    }
} 