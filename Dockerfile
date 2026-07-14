# ===== STAGE 1: Build (derleme) =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Projeyi derle ve jar adını doğrula
RUN mvn -B -DskipTests package \
    && ls -la target/ \
    && test -f target/analyzer-0.0.1-SNAPSHOT.jar

# ===== STAGE 2: Runtime (çalıştırma) =====
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/analyzer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
