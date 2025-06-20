package com.lowcode.workflowservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowStepDto {
    private Long id;
    private Integer stepOrder;
    private String actionType;
    private Map<String, Object> config;
} 