# Warehouse Management System

## Tech Stack

- Spring Boot 3.4.2
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL
- Log4j2
- Swagger/OpenAPI
- Temporal.io workflows
- Docker Compose

## Quick Start

```bash
# Run application
mvn spring-boot:run
```
```bash
# Graceful shutdown
curl 'http://localhost:8081/actuator/shutdown' -i -X POST
```

## Access Points

| Service | URL | Notes                |
|---------|-----|----------------------|
| API | http://localhost:8081 | Main application     |
| Swagger UI | http://localhost:8081/swagger-ui.html | API documentation    |
| Temporal UI | http://localhost:8088 | Workflow monitoring  |
| Jaeger | http://localhost:16686 | Distributed tracing  |
| Grafana | http://localhost:8085 | Metrics dashboards   |
| Prometheus | http://localhost:9090 | Metrics queries |
| Prometheus | http://localhost:8081/actuator/prometheus | Prometheus Metrics     |
| Worker Info | http://localhost:8081/actuator/temporalworkerinfo | Temporal worker info |

## Demo Users

All passwords: `password123`

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin1 | password123 | SYSTEM_ADMIN | User management |
| manager1 | password123 | WAREHOUSE_MANAGER | Orders, items, trucks, deliveries |
| client1 | password123 | CLIENT | Create/manage orders |


## Order Status Flow

```
CREATED
   ↓ (client submits)
AWAITING_APPROVAL
   ↓ (manager approves)         ↓ (manager declines)
APPROVED                        DECLINED
   ↓ (manager schedules)           ↓ (client can update & resubmit)
UNDER_DELIVERY                  AWAITING_APPROVAL
   ↓ (cronjob on delivery date)
FULFILLED

CANCELED can happen from any status except FULFILLED, UNDER_DELIVERY, or CANCELED
```