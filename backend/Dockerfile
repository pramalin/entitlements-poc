# Build stage - same pom.xml that already builds cleanly via `mvn package` on the host.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# NOTE: deliberately NOT using a separate `mvn dependency:go-offline` layer for
# caching - that goal is known to miss some test-scoped transitive dependencies
# (JUnit/AssertJ/Mockito went missing here), causing testCompile to fail even
# though the same pom.xml builds fine with a single `mvn package` on the host.
# One combined step is slower to rebuild but actually correct.
RUN mvn -B package -DskipTests

# Run stage - JRE only, no Maven/JDK bloat in the final image.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/entitlements-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
