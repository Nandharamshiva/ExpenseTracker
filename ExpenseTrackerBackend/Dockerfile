# Multi-stage build for Spring Boot backend
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml
COPY .mvn ./.mvn
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY src ./src

# Build the application
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -Dcompile.source=21 -Dcompile.target=21

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/ExpenseTracker-*.jar app.jar

# Install curl for healthchecks
RUN apk add --no-cache curl

# Expose port 8081 (matches server.port)
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
