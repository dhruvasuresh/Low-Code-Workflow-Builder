package com.lowcode.workflowservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkflowDto {
    private Long id;
    private String name;
    private Integer version;
    private String createdBy;
    private LocalDateTime createdAt;
    private String triggerType;
} 