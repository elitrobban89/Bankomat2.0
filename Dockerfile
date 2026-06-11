FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY web/pom.xml .
RUN mvn dependency:go-offline -B
COPY web/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/bankomat-web-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
