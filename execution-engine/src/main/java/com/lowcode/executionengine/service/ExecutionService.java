package com.lowcode.executionengine.service;

import com.lowcode.executionengine.dto.WorkflowExecutionDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.workflow-service.url}")
    private String workflowServiceUrl;

    public void executeWorkflow(String workflowId, String token) {
        logger.info("Starting execution for workflow: {}", workflowId);

        // 1. Call workflow-service to create a new WorkflowExecution
        String url = workflowServiceUrl + "/workflows/" + workflowId + "/executions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<WorkflowExecutionDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                WorkflowExecutionDto.class
        );
        WorkflowExecutionDto execution = response.getBody();
        logger.info("Created WorkflowExecution: {}", execution);

        // TODO: Fetch workflow steps and execute them

        logger.info("Finished execution for workflow: {}", workflowId);
    }
} 