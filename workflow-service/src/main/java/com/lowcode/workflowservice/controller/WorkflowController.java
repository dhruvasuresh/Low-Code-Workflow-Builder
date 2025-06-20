package com.lowcode.workflowservice.controller;

import com.lowcode.workflowservice.dto.WorkflowDto;
import com.lowcode.workflowservice.dto.WorkflowExecutionDto;
import com.lowcode.workflowservice.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowDto dto) {
        return ResponseEntity.ok(workflowService.createWorkflow(dto));
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
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/executions")
    public ResponseEntity<WorkflowExecutionDto> createWorkflowExecution(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.createWorkflowExecution(id));
    }
} 