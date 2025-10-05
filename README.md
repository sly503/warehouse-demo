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

## Access Points

| Service | URL | Notes |
|---------|-----|-------|
| API | http://localhost:8081 | Main application |
| Swagger UI | http://localhost:8081/swagger-ui.html | API documentation |
| Temporal UI | http://localhost:8088 | Workflow monitoring |
| Jaeger | http://localhost:16686 | Distributed tracing |
| Grafana | http://localhost:8085 | Metrics dashboards |
| Prometheus | http://localhost:9090 | Metrics queries |

## Demo Users

All passwords: `password123`

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin1 | password123 | SYSTEM_ADMIN | User management |
| manager1 | password123 | WAREHOUSE_MANAGER | Orders, items, trucks, deliveries |
| client1 | password123 | CLIENT | Create/manage orders |

## Notes

- Database auto-creates schema on startup (`ddl-auto: create-drop`)
- Sample data loaded from `data.sql`
- Cronjob runs daily at 00:01 AM
- Logs: `logs/warehouse-app.log`

## Troubleshooting

**Port conflicts:**
- MySQL: 3307
- App: 8081
- Temporal: 7233, 8088
- Prometheus: 9090
- Grafana: 8085
- Jaeger: 16686

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```