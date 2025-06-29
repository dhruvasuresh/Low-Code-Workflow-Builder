package com.lowcode.actionrunner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class ActionService {
    @Autowired
    private JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    public String executeAction(String actionType, Object configObj) {
        if (configObj == null || !(configObj instanceof Map)) {
            log.warn("[ActionRunner] Invalid config for action: {}", configObj);
            throw new IllegalArgumentException("Config must be a JSON object");
        }
        if ("send_email".equalsIgnoreCase(actionType) || "email".equalsIgnoreCase(actionType)) {
            Map<Object, Object> config = (Map<Object, Object>) configObj;
            String to = String.valueOf(config.get("to"));
            String subject = String.valueOf(config.get("subject"));
            String body = String.valueOf(config.getOrDefault("body", ""));
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("[ActionRunner] Sent real email to: {} | subject: {}", to, subject);
                return "Email sent to " + to + " with subject '" + subject + "'";
            } catch (Exception e) {
                log.error("[ActionRunner] Failed to send email", e);
                return "Failed to send email: " + e.getMessage();
            }
        } else if ("http_request".equalsIgnoreCase(actionType) || "http".equalsIgnoreCase(actionType)) {
            Map<Object, Object> config = (Map<Object, Object>) configObj;
            String url = String.valueOf(config.get("url"));
            String method = String.valueOf(config.getOrDefault("method", "GET"));
            String body = String.valueOf(config.getOrDefault("body", ""));
            try {
                if ("POST".equalsIgnoreCase(method)) {
                    String response = restTemplate.postForObject(url, body, String.class);
                    log.info("[ActionRunner] HTTP POST to {}: {}", url, response);
                    return "HTTP POST to " + url + ": " + response;
                } else {
                    String response = restTemplate.getForObject(url, String.class);
                    log.info("[ActionRunner] HTTP GET to {}: {}", url, response);
                    return "HTTP GET to " + url + ": " + response;
                }
            } catch (Exception e) {
                log.error("[ActionRunner] HTTP request failed", e);
                return "HTTP request failed: " + e.getMessage();
            }
        } else if ("log".equalsIgnoreCase(actionType)) {
            Map<Object, Object> config = (Map<Object, Object>) configObj;
            String message = String.valueOf(config.getOrDefault("message", ""));
            log.info("[ActionRunner] Log action: {}", message);
            return "Logged message: " + message;
        } else {
            log.warn("[ActionRunner] Unknown action type: {}", actionType);
            return "Unknown action type: " + actionType;
        }
    }
}