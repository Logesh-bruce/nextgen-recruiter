# =============================================================
# HireFlow AI — Production Multi-stage Dockerfile
# Stage 1: Build JAR using Maven & OpenJDK 21
# Stage 2: Minimal Distroless JRE 21 Runtime Image
# =============================================================

FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production executable JAR
COPY src ./src
RUN mvn package -DskipTests -B

# ── Runtime Stage ──────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S hireflow && adduser -S hireflow -G hireflow
USER hireflow:hireflow

# Copy built JAR artifact from builder stage
COPY --from=builder /app/target/hireflow-api-1.0.0.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Environment variables with sensible production defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
