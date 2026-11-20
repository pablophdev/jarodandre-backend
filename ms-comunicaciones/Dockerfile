FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --create-home app
RUN mkdir -p /app/data/keys /app/data/imagenes && chown -R app:app /app
WORKDIR /app
COPY --from=build --chown=app:app /build/target/ms-comunicaciones-0.1.0.jar app.jar
USER app
EXPOSE 8084
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65", "-jar", "app.jar"]
