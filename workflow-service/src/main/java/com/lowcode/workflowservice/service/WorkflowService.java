package com.lowcode.workflowservice.service;

import com.lowcode.workflowservice.domain.Workflow;
import com.lowcode.workflowservice.dto.WorkflowDto;
import com.lowcode.workflowservice.repository.WorkflowRepository;
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

    public WorkflowDto createWorkflow(WorkflowDto dto) {
        Workflow workflow = new Workflow();
        BeanUtils.copyProperties(dto, workflow);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setVersion(1);
        Workflow saved = workflowRepository.save(workflow);
        return toDto(saved);
    }

    public List<WorkflowDto> getAllWorkflows() {
        return workflowRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<WorkflowDto> getWorkflowById(Long id) {
        return workflowRepository.findById(id).map(this::toDto);
    }

    public Optional<WorkflowDto> updateWorkflow(Long id, WorkflowDto dto) {
        return workflowRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setTriggerType(dto.getTriggerType());
            existing.setVersion(existing.getVersion() + 1);
            Workflow updated = workflowRepository.save(existing);
            return toDto(updated);
        });
    }

    public void deleteWorkflow(Long id) {
        workflowRepository.deleteById(id);
    }

    private WorkflowDto toDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        BeanUtils.copyProperties(workflow, dto);
        return dto;
    }
} 