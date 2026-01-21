# ---------- BUILD STAGE ----------
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Copy only build files first (better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle


# 🔑 give execute permission
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
