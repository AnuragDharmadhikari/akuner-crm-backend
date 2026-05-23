# ── Stage 1: Build ────────────────────────────────────────────
# Use the full JDK with Maven to compile and package the application
# This stage is heavy but gets discarded — it never reaches production
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first — Docker caches this layer separately
# If pom.xml hasn't changed, Maven dependencies are not re-downloaded
# This is the most important Docker optimization for Java projects
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source code and build
# -DskipTests — tests run in CI pipeline, not during Docker build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────
# Use only JRE — no compiler, no Maven, no source code
# eclipse-temurin:21-jre-alpine is the smallest production-grade JRE image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user — never run production apps as root
# If the container is compromised, attacker gets nobody, not root
RUN addgroup -S vedpharm && adduser -S vedpharm -G vedpharm

# Set working directory
WORKDIR /app

# Copy only the fat JAR from the build stage
# Nothing else — no source, no Maven, no JDK
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown vedpharm:vedpharm app.jar

# Switch to non-root user
USER vedpharm

# Expose the application port
EXPOSE 8080

# JVM flags for production:
# -XX:+UseContainerSupport — JVM respects Docker memory limits
# -XX:MaxRAMPercentage=75 — use 75% of container RAM for heap
# -Djava.security.egd — faster startup, uses /dev/urandom not /dev/random
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]