package com.lowcode.workflowservice.service;

import com.lowcode.workflowservice.domain.Workflow;
import com.lowcode.workflowservice.domain.WorkflowStep;
import com.lowcode.workflowservice.dto.WorkflowStepDto;
import com.lowcode.workflowservice.repository.WorkflowRepository;
import com.lowcode.workflowservice.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowStepService {
    private final WorkflowStepRepository stepRepository;
    private final WorkflowRepository workflowRepository;

    public WorkflowStepDto addStep(Long workflowId, WorkflowStepDto dto) {
        if (stepRepository.existsByWorkflowIdAndStepOrder(workflowId, dto.getStepOrder())) {
            throw new RuntimeException("Step order must be unique within a workflow");
        }
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
        WorkflowStep step = WorkflowStep.builder()
                .workflow(workflow)
                .stepOrder(dto.getStepOrder())
                .actionType(dto.getActionType())
                .config(dto.getConfig())
                .build();
        step = stepRepository.save(step);
        dto.setId(step.getId());
        return dto;
    }

    public WorkflowStepDto updateStep(Long workflowId, Long stepId, WorkflowStepDto dto) {
        WorkflowStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found"));
        if (!step.getWorkflow().getId().equals(workflowId)) {
            throw new RuntimeException("Step does not belong to the workflow");
        }
        if (!step.getStepOrder().equals(dto.getStepOrder()) && stepRepository.existsByWorkflowIdAndStepOrder(workflowId, dto.getStepOrder())) {
            throw new RuntimeException("Step order must be unique within a workflow");
        }
        step.setStepOrder(dto.getStepOrder());
        step.setActionType(dto.getActionType());
        step.setConfig(dto.getConfig());
        step = stepRepository.save(step);
        BeanUtils.copyProperties(step, dto);
        return dto;
    }

    public void deleteStep(Long workflowId, Long stepId) {
        WorkflowStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found"));
        if (!step.getWorkflow().getId().equals(workflowId)) {
            throw new RuntimeException("Step does not belong to the workflow");
        }
        stepRepository.delete(step);
    }

    public List<WorkflowStepDto> listSteps(Long workflowId) {
        return stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId)
                .stream()
                .map(step -> {
                    WorkflowStepDto dto = new WorkflowStepDto();
                    BeanUtils.copyProperties(step, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 