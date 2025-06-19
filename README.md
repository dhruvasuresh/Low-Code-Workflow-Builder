# Low-Code Workflow Builder (Spring Boot Microservices)

This project is a Zapier-like low-code workflow automation platform built using Spring Boot microservices architecture.

## Project Structure

- **api-gateway/**: Spring Cloud Gateway for routing, security, and entry point for all clients.
- **workflow-service/**: Manages workflow definitions (CRUD operations).
- **execution-engine/**: Executes workflows and manages execution state.
- **action-runner/**: Executes individual workflow steps (actions).
- **user-service/**: Handles authentication and user management.
- **common/**: Shared code (DTOs, utilities, enums, etc.).

## Why Microservices?
- **Scalability**: Each service can be scaled independently.
- **Flexibility**: Different technologies can be used for different services.
- **Resilience**: Failure in one service does not bring down the whole system.
- **Faster Development**: Teams can work in parallel on different services.
- **Easier Maintenance**: Smaller codebases are easier to understand and maintain.

## Challenges
- **Complexity**: More moving parts, more complex deployment and monitoring.
- **Distributed Data Management**: Ensuring data consistency is harder.
- **Inter-Service Communication**: Requires robust communication patterns (REST, messaging, etc.).
- **Testing**: End-to-end testing is more complex.

## Next Steps
- Set up submodules for each microservice.
- Add Docker Compose for local orchestration.

## Database Design

### Schema Overview

We'll use PostgreSQL and the following tables:

```sql
workflow (
  id SERIAL PRIMARY KEY,
  name VARCHAR,
  version INT,
  created_by VARCHAR,
  created_at TIMESTAMP,
  trigger_type VARCHAR
)

workflow_step (
  id SERIAL PRIMARY KEY,
  workflow_id INT REFERENCES workflow(id),
  step_order INT,
  action_type VARCHAR,
  config JSONB
)

workflow_execution (
  id SERIAL PRIMARY KEY,
  workflow_id INT REFERENCES workflow(id),
  status VARCHAR,
  started_at TIMESTAMP,
  ended_at TIMESTAMP
)

step_execution (
  id SERIAL PRIMARY KEY,
  workflow_execution_id INT REFERENCES workflow_execution(id),
  step_id INT REFERENCES workflow_step(id),
  status VARCHAR,
  error_log TEXT,
  retry_count INT
)
```

### Implementation Plan

1. **Add PostgreSQL to Docker Compose** for local development.
2. **Add Spring Data JPA and PostgreSQL dependencies** to `workflow-service`.
3. **Create JPA entities** in `workflow-service` for the above tables, with proper relationships.
4. **Configure application properties** for database connection.

### How This Fits the Big Picture

- This schema supports versioned, auditable, and reliable workflow execution.
- Each microservice can have its own database (recommended for microservices), but for now, we'll focus on `workflow-service` for workflow definitions and execution tracking.

Would you like to:
- See the **Docker Compose file** for PostgreSQL and all services?
- Or jump straight to **JPA entity code** for `workflow-service`?

Let me know your preference, or I can proceed step-by-step starting with Docker Compose! 