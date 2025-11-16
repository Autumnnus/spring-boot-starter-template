# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the entire project
COPY . .
# Package the application, skipping tests
RUN mvn -B -DskipTests package

# Final stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the application JAR from the build stage
COPY --from=build /app/target/spring-boot-starter-template-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
