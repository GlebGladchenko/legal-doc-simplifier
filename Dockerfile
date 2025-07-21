# Use an official Gradle image to build the app
FROM gradle:8.5.0-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build -x test

# Use a minimal JDK image to run the app
FROM eclipse-temurin:17-jre

# Install ffmpeg and python
RUN apt-get update && apt-get install -y \
    ffmpeg \
    python3 \
    python3-pip \
    python3-venv \
    git \
    curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["java", "-jar", "/app/app.jar"]