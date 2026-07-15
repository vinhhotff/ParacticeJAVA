# Project Structure & Setup Rules

> Quy chuẩn cấu trúc project và hướng dẫn setup cho Spring Boot project.

---

## 1. Directory Structure

```
resource-allocation-management/
├── pom.xml
├── README.md
├── sql/
│   ├── 01-create-schema.sql        # CREATE TABLE + FK + indexes
│   └── 02-seed-data.sql            # Sample data
├── postman/
│   └── ResourceAllocation.postman_collection.json
├── screenshots/
│   └── *.png                       # API screenshots for submission
├── docker/
│   ├── Dockerfile                  # Containerize Spring Boot app
│   └── docker-compose.yml          # App + PostgreSQL
└── src/
    ├── main/
    │   ├── java/com/company/project/
    │   │   ├── HomeworkApplication.java         # @SpringBootApplication
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI config
    │   │   │   └── AllocationProperties.java    # @ConfigurationProperties
    │   │   ├── controller/
    │   │   │   ├── EmployeeController.java
    │   │   │   ├── ProjectController.java
    │   │   │   ├── AllocationController.java
    │   │   │   └── ReportController.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── EmployeeRequest.java
    │   │   │   │   ├── ProjectRequest.java
    │   │   │   │   └── AllocationRequest.java
    │   │   │   └── response/
    │   │   │       ├── EmployeeResponse.java
    │   │   │       ├── ProjectResponse.java
    │   │   │       ├── AllocationResponse.java
    │   │   │       ├── WorkloadResponse.java
    │   │   │       ├── UtilizationReportItem.java
    │   │   │       ├── AvailableResourceItem.java
    │   │   │       ├── OverloadedEmployeeItem.java
    │   │   │       ├── ErrorResponse.java
    │   │   │       └── ValidationDetail.java
    │   │   ├── entity/
    │   │   │   ├── Employee.java
    │   │   │   ├── Project.java
    │   │   │   ├── Allocation.java
    │   │   │   └── enums/
    │   │   │       └── ProjectStatus.java
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java   (abstract)
    │   │   │   ├── EmployeeNotFoundException.java
    │   │   │   ├── ProjectNotFoundException.java
    │   │   │   ├── AllocationNotFoundException.java
    │   │   │   ├── AllocationExceededException.java
    │   │   │   └── ProjectCompletedException.java
    │   │   ├── handler/
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   │   ├── EmployeeRepository.java
    │   │   │   ├── ProjectRepository.java
    │   │   │   └── AllocationRepository.java
    │   │   ├── service/
    │   │   │   ├── EmployeeService.java              (interface)
    │   │   │   ├── ProjectService.java               (interface)
    │   │   │   ├── AllocationService.java            (interface)
    │   │   │   ├── ReportService.java                (interface)
    │   │   │   └── impl/
    │   │   │       ├── EmployeeServiceImpl.java
    │   │   │       ├── ProjectServiceImpl.java
    │   │   │       ├── AllocationServiceImpl.java
    │   │   │       └── ReportServiceImpl.java
    │   │   ├── validator/
    │   │   │   ├── AllocationValidator.java          (interface)
    │   │   │   ├── MaxAllocationValidator.java       (Rule 2)
    │   │   │   ├── ProjectStatusValidator.java       (Rule 3)
    │   │   │   └── AllocationValidationOrchestrator.java
    │   │   └── ai/                                   (Bonus)
    │   │       ├── ResourceRecommendationService.java
    │   │       └── RiskDetectionService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application.yml                       (preferred)
    │       └── logback-spring.xml                    (optional)
    └── test/
        └── java/com/company/project/
            ├── HomeworkApplicationTests.java
            ├── controller/
            │   ├── EmployeeControllerTest.java
            │   ├── AllocationControllerTest.java
            │   └── ...
            ├── service/
            │   ├── AllocationServiceTest.java
            │   └── ...
            └── repository/
                └── AllocationRepositoryTest.java
```

---

## 2. Initial Project Setup

### 2.1 Spring Initializr Configuration

| Setting | Value |
|---------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2+ |
| Group | `com.company` |
| Artifact | `project` |
| Java Version | 17 |
| Packaging | Jar |

### 2.2 Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Swagger / OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.3 application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/resource_allocation
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate        # Use SQL scripts, not auto-create
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    open-in-view: false          # Quan trọng: tránh lazy loading issue

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

app:
  allocation:
    max-percent: 100
```

> **`open-in-view: false`**: Bắt buộc. OSIV (Open Session In View) mặc định là true trong Spring Boot, gây ra nhiều vấn đề performance và lazy loading. Tắt đi và quản lý transaction rõ ràng trong Service layer.

---

## 3. Maven Build Configuration

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>

    </plugins>
</build>
```

---

## 4. README.md Template

```markdown
# Resource Allocation Management System

Hệ thống quản lý phân bổ nhân sự cho công ty outsourcing.

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Maven
- Swagger/OpenAPI
- Docker (optional)

## Setup

### Prerequisites
- JDK 17+
- Maven 3.8+
- PostgreSQL 15+

### Database Setup
```sql
-- Run sql/01-create-schema.sql
-- Run sql/02-seed-data.sql
\i sql/01-create-schema.sql
\i sql/02-seed-data.sql
```

### Run Application
```bash
mvn clean install
mvn spring-boot:run
```

### Access
- Application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## API Endpoints

### Employee
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/employees | Create employee |
| GET    | /api/employees | List employees |
| GET    | /api/employees/{id} | Get employee |
| GET    | /api/employees/{id}/workload | Get workload |

### Project
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/projects | Create project |
| GET    | /api/projects | List projects |
| GET    | /api/projects/{id} | Get project |

### Allocation
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/allocations | Create allocation |
| PUT    | /api/allocations/{id} | Update allocation |
| DELETE | /api/allocations/{id} | Remove allocation |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/reports/utilization | Utilization report |
| GET    | /api/reports/available-resources | Available resources |
| GET    | /api/reports/overloaded | Overloaded employees |

## Business Rules
1. Allocation must be between 1% and 100%
2. Total employee allocation cannot exceed 100%
3. Cannot allocate to COMPLETED projects
```

---

## 5. IDE & Editor Configuration

### 5.1 `.editorconfig`
```ini
root = true

[*]
indent_style = space
indent_size = 4
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.md]
trim_trailing_whitespace = false
```

### 5.2 IntelliJ IDEA Settings
- **Code Style**: Import `EditorConfig` file.

---

## 6. Build & Run Commands

| Command | Description |
|---------|-------------|
| `mvn clean install` | Build project |
| `mvn spring-boot:run` | Run application |
| `mvn test` | Run tests |
| `mvn clean package -DskipTests` | Build JAR without tests |
| `java -jar target/project-0.0.1-SNAPSHOT.jar` | Run JAR file |
