package com.lowcode.executionengine.service;

import com.lowcode.executionengine.dto.WorkflowExecutionDto;
import com.lowcode.executionengine.dto.WorkflowStepDto;
import com.lowcode.common.dto.StepExecutionDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Value("${service-user.email}")
    private String serviceUserEmail;
    @Value("${service-user.password}")
    private String serviceUserPassword;

    private String jwtToken = null;
    private long jwtTokenExpiry = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        loginAndStoreJwt();
    }

    private void loginAndStoreJwt() {
        try {
            String loginUrl = workflowServiceUrl.replace(":8082", ":8081") + "/auth/login";
            Map<String, String> loginRequest = Map.of(
                "username", serviceUserEmail,
                "password", serviceUserPassword
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, loginRequest, Map.class);
            Map body = response.getBody();
            if (body != null && body.containsKey("token")) {
                jwtToken = "Bearer " + body.get("token");
                // Optionally parse expiry from token if available
                logger.info("[ExecutionEngine] Successfully logged in as service user");
            } else {
                logger.error("[ExecutionEngine] Failed to retrieve JWT from login response");
            }
        } catch (Exception ex) {
            logger.error("[ExecutionEngine] Service user login failed: {}", ex.getMessage());
        }
    }

    private String getJwtToken() {
        if (jwtToken == null) {
            loginAndStoreJwt();
        }
        return jwtToken;
    }

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
            ResponseEntity<StepExecutionDto> stepExecResponse = restTemplate.exchange(
                createStepExecUrl,
                HttpMethod.POST,
                request,
                StepExecutionDto.class
            );
            StepExecutionDto stepExec = stepExecResponse.getBody();
            Long stepExecutionId = stepExec.getId();

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
                // Parse response as JSON
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    Map<String, Object> actionResult = objectMapper.readValue(actionResponse.getBody(), Map.class);
                    if (!"success".equals(actionResult.get("status"))) {
                        status = "FAILED";
                        errorLog = String.valueOf(actionResult.get("error"));
                        finalStatus = "FAILED";
                    }
                } catch (Exception parseEx) {
                    status = "FAILED";
                    errorLog = "Invalid JSON from action-runner: " + parseEx.getMessage();
                    finalStatus = "FAILED";
                    logger.error("Failed to parse action-runner response as JSON", parseEx);
                }
            } catch (Exception ex) {
                status = "FAILED";
                errorLog = ex.getMessage();
                logger.error("Action runner failed", ex);
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
                StepExecutionDto.class
            );
        }

        // 4. Set WorkflowExecution status to COMPLETED or FAILED
        String endExecStatusUrl = workflowServiceUrl + "/workflows/executions/" + execution.getId() + "/status?status=" + finalStatus;
        restTemplate.exchange(endExecStatusUrl, HttpMethod.PUT, request, Map.class);

        logger.info("Finished execution for workflow: {}", workflowId);
    }

    @Scheduled(fixedDelay = 10000)
    public void pollAndExecutePendingWorkflows() {
        try {
            String token = getJwtToken();
            String pendingExecsUrl = workflowServiceUrl + "/workflows/executions?status=PENDING";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<WorkflowExecutionDto[]> response = restTemplate.exchange(
                    pendingExecsUrl,
                    HttpMethod.GET,
                    request,
                    WorkflowExecutionDto[].class
            );
            List<WorkflowExecutionDto> pendingExecutions = Arrays.asList(response.getBody());
            for (WorkflowExecutionDto exec : pendingExecutions) {
                executeWorkflow(exec.getWorkflowId().toString(), token);
            }
        } catch (Exception ex) {
            if (ex.getMessage() != null && (ex.getMessage().contains("403") || ex.getMessage().contains("401"))) {
                logger.warn("[ExecutionEngine] JWT may be expired or invalid, re-authenticating...");
                loginAndStoreJwt();
            } else {
                logger.error("Error polling/executing workflows: {}", ex.getMessage());
            }
        }
    }
} 