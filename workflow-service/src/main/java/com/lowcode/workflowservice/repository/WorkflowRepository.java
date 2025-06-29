package com.lowcode.workflowservice.repository;

import com.lowcode.workflowservice.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByCreatedBy(String createdBy);
} 