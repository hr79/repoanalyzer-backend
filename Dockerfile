# Base image: JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy application JAR file (excluding plain JAR)
COPY build/libs/project-ai-analyzer-*-SNAPSHOT.jar app.jar

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default container port
EXPOSE 8080

