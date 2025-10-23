FROM gradle:8.10-jdk21 AS builder
WORKDIR /workspace
COPY ../ .
RUN gradle :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
