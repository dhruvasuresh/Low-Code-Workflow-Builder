package com.lowcode.common.dto;

import lombok.Data;

@Data
public class StepExecutionDto {
    private Long id;
    private Long workflowExecutionId;
    private Long stepId;
    private String status;
    private String errorLog;
    private Integer retryCount;
} 