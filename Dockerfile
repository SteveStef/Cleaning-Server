# Stage 1: Build the application using Maven with JDK 17
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the code and build (skipping tests)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image using a lightweight Java 17 JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
