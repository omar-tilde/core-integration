# ============================================================================
#  Multi-stage Dockerfile for the core-integration service.
#
#  Build:  docker build -t core-integration:1.0.0 .
#  Run:    docker run --rm -p 8080:8080 core-integration:1.0.0
# ============================================================================

# ---- Build stage ------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Cache dependencies first for better layer reuse
COPY pom.xml .
COPY domain/pom.xml        domain/
COPY application/pom.xml   application/
COPY infrastructure/pom.xml infrastructure/
COPY presentation/pom.xml  presentation/
COPY main/pom.xml          main/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -ntp -DskipTests dependency:go-offline

# Now bring in the sources and build
COPY domain/        domain/
COPY application/   application/
COPY infrastructure/ infrastructure/
COPY presentation/  presentation/
COPY main/          main/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copy the bootable jar from the build stage
COPY --from=build /workspace/main/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]
