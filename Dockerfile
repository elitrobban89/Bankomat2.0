# Java 27 (GA 2026-09-15). Temurin har annu inga 27-avbildningar publicerade
# (eclipse-temurin:27-jdk/jre och maven:3.9-eclipse-temurin-27 ger alla 404 pa Docker Hub,
# kontrollerat 2026-09-22), sa bygget gar pa Liberica - samma OpenJDK 27, annan leverantor.
# Maven kommer fran wrappern i repots ROT (bygget kopierar web/pom.xml som sin pom, men
# wrappern ar pom-oberoende - den hamtar bara Maven). Byt tillbaka till Temurin nar deras
# 27 dyker upp: det ar ett namnbyte pa tva rader.
FROM bellsoft/liberica-openjdk-debian:27 AS build
WORKDIR /app
COPY web/pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY web/src ./src
RUN ./mvnw clean package -DskipTests

FROM bellsoft/liberica-openjre-alpine:27
WORKDIR /app
COPY --from=build /app/target/bankomat-web-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
