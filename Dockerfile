# Use an official Gradle image to build the app
FROM gradle:8.5.0-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build -x test

# Use a minimal JDK image to run the app
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["java", "-jar", "/app/app.jar"]