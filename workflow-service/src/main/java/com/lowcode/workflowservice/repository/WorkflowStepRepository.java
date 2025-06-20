package com.lowcode.workflowservice.repository;

import com.lowcode.workflowservice.domain.WorkflowStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByWorkflowIdOrderByStepOrderAsc(Long workflowId);
    boolean existsByWorkflowIdAndStepOrder(Long workflowId, Integer stepOrder);
    Page<WorkflowStep> findByWorkflowId(Long workflowId, Pageable pageable);
} 