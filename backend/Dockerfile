# =============================================================================
# ServiceHub Backend — Multi-Stage Docker Build
# =============================================================================
# Stage 1: Build the application JAR using Maven
# Stage 2: Create a minimal runtime image with only the JAR
#
# Usage:
#   docker build -t servicehub-backend ./backend
#   docker run -p 8080:8080 servicehub-backend
#
# Java version: 17 (as per project specification)
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Build
# ---------------------------------------------------------------------------
# Use the official Maven image with Eclipse Temurin JDK 17 for compilation.
# This stage is discarded after the JAR is built, keeping the final image small.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the POM first to leverage Docker layer caching for dependencies.
# Dependencies only re-download when pom.xml changes, not on every code change.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application.
COPY src ./src
RUN mvn package -DskipTests -B

# ---------------------------------------------------------------------------
# Stage 2: Runtime
# ---------------------------------------------------------------------------
# Use the JRE-only Alpine image for a minimal footprint (~180MB vs ~500MB).
# Alpine reduces the attack surface by excluding unnecessary system packages.
FROM eclipse-temurin:17-jre-alpine

# Security: Run as a non-root user to follow the principle of least privilege.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the built JAR from the build stage.
COPY --from=build /app/target/*.jar app.jar

# Switch to non-root user.
USER appuser

# Document the port the application listens on.
EXPOSE 8080

# Health check: Verify the application is responding.
# Uses wget instead of curl because Alpine doesn't include curl by default.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning: Respect container memory limits and use container-aware defaults.
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
