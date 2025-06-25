package com.lowcode.executionengine.service;

import com.lowcode.executionengine.dto.WorkflowExecutionDto;
import com.lowcode.executionengine.dto.WorkflowStepDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.workflow-service.url}")
    private String workflowServiceUrl;

    @Value("${services.action-runner.url:http://localhost:8084}")
    private String actionRunnerUrl;

    public void executeWorkflow(String workflowId, String token) {
        logger.info("Starting execution for workflow: {}", workflowId);

        // 1. Create a new WorkflowExecution
        String execUrl = workflowServiceUrl + "/workflows/" + workflowId + "/executions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<WorkflowExecutionDto> response = restTemplate.exchange(
                execUrl,
                HttpMethod.POST,
                request,
                WorkflowExecutionDto.class
        );
        WorkflowExecutionDto execution = response.getBody();
        logger.info("Created WorkflowExecution: {}", execution);

        // 1. Set WorkflowExecution status to IN_PROGRESS
        String updateExecStatusUrl = workflowServiceUrl + "/workflows/executions/" + execution.getId() + "/status?status=IN_PROGRESS";
        restTemplate.exchange(updateExecStatusUrl, HttpMethod.PUT, request, Map.class);

        // 2. Fetch workflow steps
        String stepsUrl = workflowServiceUrl + "/workflows/" + workflowId + "/steps";
        ResponseEntity<WorkflowStepDto[]> stepsResponse = restTemplate.exchange(
                stepsUrl,
                HttpMethod.GET,
                request,
                WorkflowStepDto[].class
        );
        List<WorkflowStepDto> steps = Arrays.asList(stepsResponse.getBody());
        logger.info("Fetched {} steps for workflow {}", steps.size(), workflowId);

        // 3. For each step, call action-runner
        String finalStatus = "COMPLETED";
        for (WorkflowStepDto step : steps) {
            logger.info("Executing step {}: {}", step.getStepOrder(), step.getActionType());
            // 1. Create StepExecution (IN_PROGRESS)
            String createStepExecUrl = UriComponentsBuilder.fromHttpUrl(workflowServiceUrl)
                .path("/workflows/" + workflowId + "/steps/executions")
                .queryParam("workflowExecutionId", execution.getId())
                .queryParam("stepId", step.getId())
                .queryParam("status", "IN_PROGRESS")
                .toUriString();
            ResponseEntity<Map> stepExecResponse = restTemplate.exchange(
                createStepExecUrl,
                HttpMethod.POST,
                request,
                Map.class
            );
            Map stepExec = stepExecResponse.getBody();
            Long stepExecutionId = Long.valueOf(String.valueOf(stepExec.get("id")));

            // 2. Call action-runner
            String actionUrl = actionRunnerUrl + "/actions/execute";
            HttpHeaders actionHeaders = new HttpHeaders();
            actionHeaders.set("Authorization", token);
            actionHeaders.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> actionRequest = Map.of(
                    "actionType", step.getActionType(),
                    "config", step.getConfig()
            );
            HttpEntity<Map<String, Object>> actionEntity = new HttpEntity<>(actionRequest, actionHeaders);
            String status = "COMPLETED";
            String errorLog = null;
            try {
                ResponseEntity<String> actionResponse = restTemplate.exchange(
                        actionUrl,
                        HttpMethod.POST,
                        actionEntity,
                        String.class
                );
                logger.info("Action runner response: {}", actionResponse.getBody());
            } catch (Exception ex) {
                status = "FAILED";
                errorLog = ex.getMessage();
                logger.error("Action runner failed: {}", ex.getMessage());
                finalStatus = "FAILED";
            }

            // 3. Update StepExecution status
            String updateStepExecUrl = UriComponentsBuilder.fromHttpUrl(workflowServiceUrl)
                .path("/workflows/" + workflowId + "/steps/executions/" + stepExecutionId)
                .queryParam("status", status)
                .queryParam("errorLog", errorLog)
                .toUriString();
            restTemplate.exchange(
                updateStepExecUrl,
                HttpMethod.PUT,
                request,
                Map.class
            );
        }

        // 4. Set WorkflowExecution status to COMPLETED or FAILED
        String endExecStatusUrl = workflowServiceUrl + "/workflows/executions/" + execution.getId() + "/status?status=" + finalStatus;
        restTemplate.exchange(endExecStatusUrl, HttpMethod.PUT, request, Map.class);

        logger.info("Finished execution for workflow: {}", workflowId);
    }
} 