FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY discounter-backend/pom.xml pom.xml
COPY discounter-backend/src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/discounter-backend-*-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
