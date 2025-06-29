package com.lowcode.executionengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExecutionEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExecutionEngineApplication.class, args);
    }
} 