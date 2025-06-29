package com.lowcode.webfrontend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WorkflowController {
    private final WebClient webClient;

    @Value("${backend.workflow-service.url:http://localhost:8080/api/workflow}")
    private String workflowServiceUrl;

    @GetMapping("/workflows")
    public String workflows(Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            List<Map> workflows = webClient.get()
                    .uri(workflowServiceUrl + "/workflows")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            model.addAttribute("workflows", workflows);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch workflows: " + ex.getMessage());
        }
        return "workflows";
    }

    @GetMapping("/workflows/create")
    public String createWorkflowForm(Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        return "create-workflow";
    }

    @PostMapping("/workflows/create")
    public String createWorkflow(@RequestParam String name, @RequestParam(required = false) String description, @RequestParam String triggerType, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            Map<String, Object> requestBody = Map.of(
                "name", name,
                "description", description == null ? "" : description,
                "triggerType", triggerType
            );
            webClient.post()
                    .uri(workflowServiceUrl + "/workflows")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return "redirect:/workflows";
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to create workflow: " + ex.getMessage());
            return "create-workflow";
        }
    }

    @GetMapping("/workflow/{id}")
    public String workflowDetails(@PathVariable Long id, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            Map workflow = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            model.addAttribute("workflow", workflow);
            // Fetch steps for this workflow
            List<Map> steps = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + id + "/steps")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            model.addAttribute("steps", steps);
            // Fetch executions for this workflow
            List<Map> executions = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + id + "/executions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            model.addAttribute("executions", executions);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch workflow details: " + ex.getMessage());
        }
        return "workflow-details";
    }

    @GetMapping("/workflow/{id}/edit")
    public String editWorkflowForm(@PathVariable Long id, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            Map workflow = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            model.addAttribute("workflow", workflow);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch workflow details: " + ex.getMessage());
        }
        return "edit-workflow";
    }

    @PostMapping("/workflow/{id}/edit")
    public String editWorkflow(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) String description, @RequestParam String triggerType, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            Map<String, Object> requestBody = Map.of(
                "name", name,
                "description", description == null ? "" : description,
                "triggerType", triggerType
            );
            webClient.put()
                    .uri(workflowServiceUrl + "/workflows/" + id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return "redirect:/workflow/" + id;
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to update workflow: " + ex.getMessage());
            model.addAttribute("workflow", Map.of("id", id, "name", name, "description", description, "triggerType", triggerType));
            return "edit-workflow";
        }
    }

    @PostMapping("/workflow/{id}/delete")
    public String deleteWorkflow(@PathVariable Long id, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            webClient.delete()
                    .uri(workflowServiceUrl + "/workflows/" + id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            // Optionally handle error
        }
        return "redirect:/workflows";
    }

    @GetMapping("/workflow/{workflowId}/steps/add")
    public String addStepForm(@PathVariable Long workflowId, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        model.addAttribute("workflowId", workflowId);
        return "add-step";
    }

    @PostMapping("/workflow/{workflowId}/steps/add")
    public String addStep(@PathVariable Long workflowId, @RequestParam Integer stepOrder, @RequestParam String actionType, @RequestParam String config, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> configMap = objectMapper.readValue(config, Map.class);
            Map<String, Object> requestBody = Map.of(
                "stepOrder", stepOrder,
                "actionType", actionType,
                "config", configMap
            );
            webClient.post()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/steps")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return "redirect:/workflow/" + workflowId;
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to add step: " + ex.getMessage());
            model.addAttribute("workflowId", workflowId);
            return "add-step";
        }
    }

    @GetMapping("/workflow/{workflowId}/steps/{stepId}/edit")
    public String editStepForm(@PathVariable Long workflowId, @PathVariable Long stepId, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            Map step = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/steps/" + stepId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            model.addAttribute("workflowId", workflowId);
            model.addAttribute("step", step);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch step: " + ex.getMessage());
            model.addAttribute("workflowId", workflowId);
        }
        return "edit-step";
    }

    @PostMapping("/workflow/{workflowId}/steps/{stepId}/edit")
    public String editStep(@PathVariable Long workflowId, @PathVariable Long stepId, @RequestParam Integer stepOrder, @RequestParam String actionType, @RequestParam String config, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> configMap = objectMapper.readValue(config, Map.class);
            Map<String, Object> requestBody = Map.of(
                "stepOrder", stepOrder,
                "actionType", actionType,
                "config", configMap
            );
            webClient.put()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/steps/" + stepId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return "redirect:/workflow/" + workflowId;
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to update step: " + ex.getMessage());
            model.addAttribute("workflowId", workflowId);
            model.addAttribute("step", Map.of("id", stepId, "stepOrder", stepOrder, "actionType", actionType, "config", config));
            return "edit-step";
        }
    }

    @PostMapping("/workflow/{workflowId}/steps/{stepId}/delete")
    public String deleteStep(@PathVariable Long workflowId, @PathVariable Long stepId, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            webClient.delete()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/steps/" + stepId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            // Optionally handle error
        }
        return "redirect:/workflow/" + workflowId;
    }

    @PostMapping("/workflow/{workflowId}/executions/run")
    public String runWorkflow(@PathVariable Long workflowId, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            webClient.post()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/executions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception ex) {
            // Optionally handle error
        }
        return "redirect:/workflow/" + workflowId;
    }

    @GetMapping("/workflow/{workflowId}/executions/{executionId}")
    public String executionDetails(@PathVariable Long workflowId, @PathVariable Long executionId, Model model, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            // Fetch execution info
            List<Map> executions = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/executions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            Map execution = executions.stream().filter(e -> executionId.equals(Long.valueOf(e.get("id").toString()))).findFirst().orElse(null);
            model.addAttribute("execution", execution);
            model.addAttribute("workflowId", workflowId);
            // Fetch step executions for this execution
            List<Map> stepExecutions = webClient.get()
                    .uri(workflowServiceUrl + "/workflows/" + workflowId + "/steps/executions/by-execution/" + executionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();
            model.addAttribute("stepExecutions", stepExecutions);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch execution details: " + ex.getMessage());
            model.addAttribute("workflowId", workflowId);
        }
        return "execution-details";
    }

    @PostMapping("/workflow/{workflowId}/executions/{executionId}/delete")
    public String deleteExecution(@PathVariable Long workflowId, @PathVariable Long executionId, HttpSession session) {
        String jwt = (String) session.getAttribute("jwt");
        if (jwt == null) {
            return "redirect:/login";
        }
        try {
            webClient.delete()
                    .uri(workflowServiceUrl + "/workflows/executions/" + executionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            // Optionally handle error
        }
        return "redirect:/workflow/" + workflowId;
    }
} 