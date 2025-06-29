package com.lowcode.workflowservice.service;

import com.lowcode.workflowservice.domain.Workflow;
import com.lowcode.workflowservice.domain.WorkflowStep;
import com.lowcode.workflowservice.dto.WorkflowStepDto;
import com.lowcode.workflowservice.repository.WorkflowRepository;
import com.lowcode.workflowservice.repository.WorkflowStepRepository;
import com.lowcode.workflowservice.domain.StepExecution;
import com.lowcode.workflowservice.domain.WorkflowExecution;
import com.lowcode.workflowservice.repository.StepExecutionRepository;
import com.lowcode.workflowservice.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.lowcode.common.dto.StepExecutionDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowStepService {
    private final WorkflowStepRepository stepRepository;
    private final WorkflowRepository workflowRepository;
    private final StepExecutionRepository stepExecutionRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;

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

    public StepExecutionDto createStepExecution(Long workflowExecutionId, Long stepId, String status) {
        WorkflowExecution execution = workflowExecutionRepository.findById(workflowExecutionId)
                .orElseThrow(() -> new RuntimeException("WorkflowExecution not found"));
        WorkflowStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("WorkflowStep not found"));
        StepExecution stepExecution = StepExecution.builder()
                .workflowExecution(execution)
                .step(step)
                .status(status)
                .retryCount(0)
                .build();
        stepExecution = stepExecutionRepository.save(stepExecution);
        return toDto(stepExecution);
    }

    public StepExecutionDto toDto(StepExecution stepExecution) {
        StepExecutionDto dto = new StepExecutionDto();
        dto.setId(stepExecution.getId());
        dto.setWorkflowExecutionId(stepExecution.getWorkflowExecution() != null ? stepExecution.getWorkflowExecution().getId() : null);
        dto.setStepId(stepExecution.getStep() != null ? stepExecution.getStep().getId() : null);
        dto.setStatus(stepExecution.getStatus());
        dto.setErrorLog(stepExecution.getErrorLog());
        dto.setRetryCount(stepExecution.getRetryCount());
        return dto;
    }

    public StepExecution updateStepExecutionStatus(Long stepExecutionId, String status, String errorLog) {
        StepExecution stepExecution = stepExecutionRepository.findById(stepExecutionId)
                .orElseThrow(() -> new RuntimeException("StepExecution not found"));
        stepExecution.setStatus(status);
        stepExecution.setErrorLog(errorLog);
        return stepExecutionRepository.save(stepExecution);
    }

    public WorkflowStepDto getStepById(Long workflowId, Long stepId) {
        Optional<WorkflowStep> stepOpt = stepRepository.findById(stepId);
        if (stepOpt.isEmpty()) return null;
        WorkflowStep step = stepOpt.get();
        if (!step.getWorkflow().getId().equals(workflowId)) return null;
        WorkflowStepDto dto = new WorkflowStepDto();
        BeanUtils.copyProperties(step, dto);
        return dto;
    }
} 