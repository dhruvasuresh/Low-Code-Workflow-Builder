package com.lowcode.workflowservice.repository;

import com.lowcode.workflowservice.domain.StepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepExecutionRepository extends JpaRepository<StepExecution, Long> {
} 