# Stage 1: Build the code on Render using Java 25
FROM maven:3.9.14-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final tiny image using Java 25 to run the app
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
# This line safely grabs the jar from the build stage above
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]