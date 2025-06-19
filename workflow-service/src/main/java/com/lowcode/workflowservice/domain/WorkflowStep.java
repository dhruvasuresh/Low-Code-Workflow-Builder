package com.lowcode.workflowservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_step")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "action_type")
    private String actionType;

    @Column(columnDefinition = "jsonb")
    private String config;
} 