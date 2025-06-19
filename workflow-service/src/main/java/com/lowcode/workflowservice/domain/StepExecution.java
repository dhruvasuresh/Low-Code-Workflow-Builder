package com.lowcode.workflowservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "step_execution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecution workflowExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private WorkflowStep step;

    private String status;
    @Column(columnDefinition = "text")
    private String errorLog;
    private Integer retryCount;
} 