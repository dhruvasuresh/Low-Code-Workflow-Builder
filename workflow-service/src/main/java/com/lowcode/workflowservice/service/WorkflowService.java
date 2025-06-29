package com.lowcode.workflowservice.service;

import com.lowcode.workflowservice.domain.Workflow;
import com.lowcode.workflowservice.domain.WorkflowExecution;
import com.lowcode.workflowservice.dto.WorkflowDto;
import com.lowcode.workflowservice.dto.WorkflowExecutionDto;
import com.lowcode.workflowservice.repository.WorkflowRepository;
import com.lowcode.workflowservice.repository.WorkflowExecutionRepository;
import com.lowcode.workflowservice.repository.StepExecutionRepository;
import com.lowcode.workflowservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final StepExecutionRepository stepExecutionRepository;

    public WorkflowDto createWorkflow(WorkflowDto dto, String username) {
        Workflow workflow = new Workflow();
        BeanUtils.copyProperties(dto, workflow);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setVersion(1);
        workflow.setCreatedBy(username);
        workflow.setDescription(dto.getDescription());
        Workflow saved = workflowRepository.save(workflow);
        return toDto(saved);
    }

    public List<WorkflowDto> getAllWorkflows() {
        return workflowRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDto> getUserWorkflows(String username) {
        return workflowRepository.findByCreatedBy(username).stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<WorkflowDto> getWorkflowById(Long id) {
        return workflowRepository.findById(id)
                .map(this::toDto);
    }

    public Optional<WorkflowDto> updateWorkflow(Long id, WorkflowDto dto) {
        return workflowRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setTriggerType(dto.getTriggerType());
            existing.setVersion(existing.getVersion() + 1);
            existing.setDescription(dto.getDescription());
            Workflow updated = workflowRepository.save(existing);
            return toDto(updated);
        });
    }

    public void deleteWorkflow(Long id) {
        workflowRepository.deleteById(id);
    }

    public WorkflowExecutionDto createWorkflowExecution(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        WorkflowExecution execution = WorkflowExecution.builder()
                .workflow(workflow)
                .status("PENDING")
                .startedAt(java.time.LocalDateTime.now())
                .build();
        execution = workflowExecutionRepository.save(execution);
        WorkflowExecutionDto dto = new WorkflowExecutionDto();
        dto.setId(execution.getId());
        dto.setWorkflowId(workflowId);
        dto.setStatus(execution.getStatus());
        dto.setStartedAt(execution.getStartedAt());
        dto.setEndedAt(execution.getEndedAt());
        return dto;
    }

    public WorkflowExecutionDto updateWorkflowExecutionStatus(Long executionId, String status) {
        WorkflowExecution execution = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowExecution not found"));
        execution.setStatus(status);
        if ("COMPLETED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
            execution.setEndedAt(java.time.LocalDateTime.now());
        }
        workflowExecutionRepository.save(execution);
        WorkflowExecutionDto dto = new WorkflowExecutionDto();
        dto.setId(execution.getId());
        dto.setWorkflowId(execution.getWorkflow().getId());
        dto.setStatus(execution.getStatus());
        dto.setStartedAt(execution.getStartedAt());
        dto.setEndedAt(execution.getEndedAt());
        return dto;
    }

    public List<WorkflowExecutionDto> getExecutionsForWorkflow(Long workflowId) {
        List<WorkflowExecution> executions = workflowExecutionRepository.findAll();
        return executions.stream()
                .filter(e -> e.getWorkflow().getId().equals(workflowId))
                .map(e -> {
                    WorkflowExecutionDto dto = new WorkflowExecutionDto();
                    dto.setId(e.getId());
                    dto.setWorkflowId(workflowId);
                    dto.setStatus(e.getStatus());
                    dto.setStartedAt(e.getStartedAt());
                    dto.setEndedAt(e.getEndedAt());
                    return dto;
                })
                .toList();
    }

    public void deleteWorkflowExecution(Long executionId) {
        stepExecutionRepository.deleteByWorkflowExecutionId(executionId);
        workflowExecutionRepository.deleteById(executionId);
    }

    public List<WorkflowExecutionDto> getExecutionsByStatus(String status) {
        List<WorkflowExecution> executions = workflowExecutionRepository.findAll();
        return executions.stream()
                .filter(e -> e.getStatus().equalsIgnoreCase(status))
                .map(e -> {
                    WorkflowExecutionDto dto = new WorkflowExecutionDto();
                    dto.setId(e.getId());
                    dto.setWorkflowId(e.getWorkflow().getId());
                    dto.setStatus(e.getStatus());
                    dto.setStartedAt(e.getStartedAt());
                    dto.setEndedAt(e.getEndedAt());
                    return dto;
                })
                .toList();
    }

    private WorkflowDto toDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        BeanUtils.copyProperties(workflow, dto);
        dto.setDescription(workflow.getDescription());
        return dto;
    }
} 