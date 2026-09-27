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
# Liberica levererar INTE JDK:ns CDS-arkiv (lib/server/classes.jsa), sa varje JDK-klass laddas
# kallt. Pa Renders gratis-CPU ligger starten da nara gransen for portskanningen: 2026-09-27
# gick en deploy igenom och nasta - med identisk kod, bara README andrad - foll ("failed deploy").
# Samma fix som BiltUthyrning 2026-09-23 (8498165): -Xshare:dump bygger arkivet en gang har,
# TieredStopAtLevel=1 (bara C1) kortar starten ytterligare pa en CPU-snal instans.
RUN java -Xshare:dump
WORKDIR /app
COPY --from=build /app/target/bankomat-web-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
