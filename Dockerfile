# Java 27 pa Eclipse Temurin. Bygget gick 2026-09-22--10-05 pa Liberica eftersom Temurin
# saknade 27-avbildningar; eclipse-temurin:27-jdk/-jre/-jre-alpine finns nu (kontrollerat 2026-10-05).
# Maven kommer fortfarande fran wrappern i repot: maven:3.9-eclipse-temurin-27 finns inte an,
# och wrappern hamtar Maven sjalv med wget, curl ELLER bara java.
# Wrappern ligger i repots ROT (bygget kopierar web/pom.xml som sin pom, men wrappern ar
# pom-oberoende - den hamtar bara Maven).
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY web/pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY web/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:27-jre-alpine
# Behalls efter bytet till Temurin (ofarligt om arkivet redan finns; startmarginalen pa Render
# ar for liten for att chansa). Bakgrund: Liberica levererade INTE JDK:ns CDS-arkiv (lib/server/classes.jsa), sa varje JDK-klass laddas
# kallt. Pa Renders gratis-CPU ligger starten da nara gransen for portskanningen: 2026-09-27
# gick en deploy igenom och nasta - med identisk kod, bara README andrad - foll ("failed deploy").
# Samma fix som BiltUthyrning 2026-09-23 (8498165): -Xshare:dump bygger arkivet en gang har,
# TieredStopAtLevel=1 (bara C1) kortar starten ytterligare pa en CPU-snal instans.
RUN java -Xshare:dump
WORKDIR /app
COPY --from=build /app/target/bankomat-web-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
