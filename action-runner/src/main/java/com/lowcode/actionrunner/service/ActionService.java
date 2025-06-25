package com.lowcode.actionrunner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ActionService {
    public String executeAction(String actionType, Object configObj) {
        if ("send_email".equalsIgnoreCase(actionType)) {
            if (configObj instanceof Map) {
                Map<Object, Object> config = (Map<Object, Object>) configObj;
                String to = String.valueOf(config.get("to"));
                String subject = String.valueOf(config.get("subject"));
                String body = String.valueOf(config.getOrDefault("body", ""));
                // Simulate sending email
                log.info("[ActionRunner] Sending email to: {} | subject: {} | body: {}", to, subject, body);
                return "Email sent to " + to + " with subject '" + subject + "'";
            } else {
                log.warn("[ActionRunner] Invalid config for send_email: {}", configObj);
                return "Invalid config for send_email";
            }
        } else if ("http_request".equalsIgnoreCase(actionType)) {
            if (configObj instanceof Map) {
                Map<Object, Object> config = (Map<Object, Object>) configObj;
                String url = String.valueOf(config.get("url"));
                String method = String.valueOf(config.getOrDefault("method", "GET"));
                // Simulate HTTP request
                log.info("[ActionRunner] Making HTTP {} request to: {}", method, url);
                return "HTTP " + method + " request sent to " + url;
            } else {
                log.warn("[ActionRunner] Invalid config for http_request: {}", configObj);
                return "Invalid config for http_request";
            }
        } else {
            log.warn("[ActionRunner] Unknown action type: {}", actionType);
            return "Unknown action type: " + actionType;
        }
    }
}