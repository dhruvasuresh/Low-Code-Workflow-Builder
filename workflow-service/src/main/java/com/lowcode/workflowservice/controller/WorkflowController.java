package com.lowcode.workflowservice.controller;

import com.lowcode.workflowservice.dto.WorkflowDto;
import com.lowcode.workflowservice.dto.WorkflowExecutionDto;
import com.lowcode.workflowservice.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    public ResponseEntity<Map<String, Object>> getAllWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowDto> workflowPage = workflowService.getAllWorkflows(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("content", workflowPage.getContent());
        response.put("page", workflowPage.getNumber());
        response.put("size", workflowPage.getSize());
        response.put("totalElements", workflowPage.getTotalElements());
        response.put("totalPages", workflowPage.getTotalPages());
        return ResponseEntity.ok(response);
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
} 