# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create app group and user (Debian style)
RUN groupadd -r app && useradd -r -g app app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Security: use non-root user
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]