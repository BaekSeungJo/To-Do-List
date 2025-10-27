# Minimal runtime image for Spring Boot 3 (Java 21)
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
