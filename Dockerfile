# =====================================================================
# Multi-stage Dockerfile for the Student Management System
#
# Stage 1 ("build"): uses a full Maven + JDK image to compile the project
#                     and produce the executable jar. This image is large
#                     but is discarded after the build - it never ships.
# Stage 2 ("runtime"): copies ONLY the built jar into a slim JRE-only
#                     image, which is much smaller and has a reduced
#                     attack surface (no build tools, no source code).
# =====================================================================

# ---------- Stage 1: Build ----------
# Bumped 21 -> 25 to match the project's new Java 25 target (pom.xml java.version).
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy pom.xml first and download dependencies separately from the source
# code. Docker caches this layer, so re-running the build after only
# changing a .java file will NOT re-download all dependencies.
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Now copy the actual source and build the jar (skip tests here - they run
# as their own explicit stage in the Jenkins pipeline / CI, see Jenkinsfile).
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
# Bumped 21 -> 25: the runtime JRE major version must be >= the bytecode
# version the jar was compiled for (25), or the container fails at startup
# with "UnsupportedClassVersionError".
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

# Run as a non-root user for better container security.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the final jar from the build stage.
COPY --from=build /app/target/student-management-system.jar app.jar

EXPOSE 8081

# Basic container-level health check hitting Actuator's health endpoint.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
