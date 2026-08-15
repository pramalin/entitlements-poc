# Build stage - identical pattern to backend/Dockerfile (single mvn invocation,
# not a separate dependency:go-offline layer - see backend/Dockerfile's comment
# for why that split caused missing test dependencies).
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# Run stage - this container is meant to run once and exit (docker compose run),
# not stay up. No EXPOSE, no restart policy here (that's controlled in compose.yaml).
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/entitlements-llm-utility.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
