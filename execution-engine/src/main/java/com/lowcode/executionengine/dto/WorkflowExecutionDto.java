package com.lowcode.executionengine.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowExecutionDto {
    private Long id;
    private Long workflowId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
} 