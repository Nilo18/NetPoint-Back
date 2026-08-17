# Stage 1: Build the code on Render
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final tiny image to run the app
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# This line safely grabs the jar from the build stage above
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]