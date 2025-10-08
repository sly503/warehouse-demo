# Warehouse Management System

## Tech Stack

- Spring Boot 3.4.2
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL
- Log4j2
- Swagger/OpenAPI
- Docker Compose

## Quick Start

```bash
# Start all services (MySQL + Spring Boot App)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

## Access Points

| Service | URL |
|---------|-----|
| API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |

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