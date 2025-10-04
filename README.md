# warehouse-demo

Temporal Spring Boot application with local Temporal server.

## Build

```bash
mvn spring-boot:run
```

## How to use
1. Application UI - http://localhost:8081
2. Temporal UI - http://localhost:8088
3. Prometheus Metrics - http://localhost:8081/actuator/prometheus
4. Grafana Dashboard - http://localhost:8085
5. Jaeger Traces - http://localhost:16686
6. Worker Info - http://localhost:8081/actuator/temporalworkerinfo

## Graceful shutdown

```bash
curl 'http://localhost:8081/actuator/shutdown' -i -X POST
```
