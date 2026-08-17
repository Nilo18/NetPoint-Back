# Stage 1: Build the code on Render using Java 25
FROM maven:3.9.14-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Change this line from alpine to jammy!
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Tells Spring Boot to scan the root folder for the application.properties file cleanly
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.additional-location=optional:file:/etc/secrets/"]