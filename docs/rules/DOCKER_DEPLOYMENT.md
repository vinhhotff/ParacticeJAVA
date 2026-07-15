# Docker & Deployment Rules

> Containerization và deployment setup cho Spring Boot + PostgreSQL.

---

## 1. Dockerfile

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app

# Copy JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

# Security: dùng non-root user
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
    CMD wget --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 2. docker-compose.yml

```yaml
version: '3.8'

services:
  app:
    build: .
    container_name: resource-allocation-app
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/resource_allocation
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped

  db:
    image: postgres:16-alpine
    container_name: resource-allocation-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: resource_allocation
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql:/docker-entrypoint-initdb.d    # Auto-run SQL scripts
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

volumes:
  postgres_data:
    driver: local
```

---

## 3. application-docker.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/resource_allocation
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

  sql:
    init:
      mode: never          # Dùng SQL script từ docker-entrypoint-initdb.d
```

---

## 4. Docker Commands

| Command | Description |
|---------|-------------|
| `docker compose up -d` | Start all services |
| `docker compose down` | Stop all services |
| `docker compose down -v` | Stop + delete volumes |
| `docker compose logs -f app` | Follow app logs |
| `docker compose exec db psql -U postgres -d resource_allocation` | Access DB |
| `docker compose up --build -d` | Rebuild + start |
| `docker compose restart app` | Restart app only |
| `docker system prune -a` | Clean all unused resources |

---

## 5. Deployment Checklist

### 5.1 Before Deploy
- [ ] `application.yml` không chứa credentials thật (dùng env vars)
- [ ] `spring.jpa.hibernate.ddl-auto=validate` (không `update` hoặc `create`)
- [ ] `spring.jpa.open-in-view=false`
- [ ] Logging level set thành `INFO` hoặc `WARN` (không `DEBUG`)
- [ ] PostgreSQL connection pool: `spring.datasource.hikari.maximum-pool-size=10`
- [ ] Health endpoint enabled: `management.endpoints.web.exposure.include=health`

### 5.2 Security Checklist
- [ ] Non-root user trong Dockerfile
- [ ] DB password không hardcode — dùng environment variables
- [ ] CORS config nếu có frontend
- [ ] Rate limiting cho API (optional, với Spring Cloud Gateway hoặc filter)

---

## 6. Production Considerations

### 6.1 JVM Options

```dockerfile
ENTRYPOINT ["java",
    "-XX:+UseContainerSupport",
    "-XX:MaxRAMPercentage=75.0",
    "-XX:+ExitOnOutOfMemoryError",
    "-jar", "app.jar"]
```

### 6.2 Environment Variables (thay hardcode)

```yaml
# docker-compose.yml
environment:
  DB_URL: jdbc:postgresql://${DB_HOST:-db}:5432/${DB_NAME:-resource_allocation}
  DB_USERNAME: ${DB_USERNAME:-postgres}
  DB_PASSWORD: ${DB_PASSWORD:-postgres}
  APP_LOG_LEVEL: ${APP_LOG_LEVEL:-INFO}
```

```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```
