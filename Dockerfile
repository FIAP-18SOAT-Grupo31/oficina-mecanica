FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S oficina && adduser -S oficina -G oficina
COPY --from=build /build/target/*.jar app.jar
USER oficina
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD nc -z localhost 8080 || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
