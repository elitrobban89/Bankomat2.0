# Bankomat 2.0

![CI](https://github.com/elitrobban89/Bankomat2.0/actions/workflows/ci.yml/badge.svg)

Ett internt bankhanteringssystem med två versioner:

- **Webbversion (live):** [elitrobban.se/bankomat-2-0/](https://elitrobban.se/bankomat-2-0/) — Spring Boot + Thymeleaf + PostgreSQL, driftsatt på Render
- **Skrivbordsversion:** Java Swing + SQLite, körs lokalt

## Funktioner

Båda versionerna har samma funktionsuppsättning och ett nästan identiskt service-lager.

**Registervård**
- Registrera nya kontoinnehavare
- Skapa nya konton (sparkonto eller lönekonto)
- Visa lista över alla kontoinnehavare
- Visa en persons alla konton med totalt saldo
- Ta bort kontoinnehavare (kräver att inga aktiva konton finns)

**Kontoöversikt**
- Lista över alla konton i banken (webbversionen: egen sida; skrivbordsversionen: tabell med totalsumma)

**Kontohantering**
- Söka upp konto med kontonummer
- Sätta in pengar
- Ta ut pengar (med snabbvalsknappar 100–5 000 kr i webbversionen)
- Överföra pengar mellan konton
- Visa scrollbar transaktionshistorik med datum och tid
- Belopp kan skrivas med decimalkomma eller decimalpunkt (100,50 eller 100.50)
- Saldo uppdateras automatiskt efter varje transaktion
- Ta bort konto (alla tillhörande transaktioner tas också bort)
- Alla penningtransaktioner körs atomärt i databastransaktioner
- Unika index på personnamn och kontonummer i båda databaserna

## Fönsterhantering

- Stänger man ett underfönster (Registervård eller Kontohantering) med X-knappen visas huvudmenyn automatiskt igen
- Alla underfönster använder `DISPOSE_ON_CLOSE` så att fönsterobjekt frigörs korrekt från minnet

## Teknisk stack

**Skrivbordsversion**

| Komponent | Teknologi |
|-----------|-----------|
| GUI | Java Swing |
| Databas | SQLite |
| JDBC-driver | sqlite-jdbc 3.7.15 |
| Java-version | Java 27 |

> **Java 27 sedan 2026-09-22** (GA 15 september). Provat på riktigt innan det byttes: ren ombyggnad på JDK 27+35, klassfilsversion **71** och gröna tester — varken Mockito eller Byte Buddy behövde röras.
>
> Bygget kör på **BellSoft Liberica 27**, inte Temurin. Liberica är samma OpenJDK 27, byggd av en annan leverantör; skälet till bytet är att Temurin ännu inte publicerat en enda 27-avbildning (`eclipse-temurin:27-jdk`, `:27-jre` och `maven:3.9-eclipse-temurin-27` svarar alla 404 på Docker Hub, och Adoptium listar `jdk-27+35` utan binärer). Att stanna på Temurin hade alltså betytt att stanna på Java 25. Byggsteget är `liberica-openjdk-debian:27` med **Maven-wrappern i repot** (3.9.16) — det finns ingen `maven`-avbildning med JDK 27 än, och wrappern hämtar Maven själv med wget, curl **eller bara java**, så den ställer inga krav på basavbildningen. Byt tillbaka till Temurin när deras 27 dyker upp: det är ett namnbyte på två rader.
**Webbversion**

| Komponent | Teknologi |
|-----------|-----------|
| Backend | Spring Boot 4.1.1 |
| Templating | Thymeleaf |
| Databas | PostgreSQL 18 (versionen läses ur anslutningen, se Uppstartsskärm) |
| Hosting | Render (Docker), autodeploy från `master` |
| Font | Share Tech Mono (Google Fonts) |
| Java-version | Java 27 |

## Webbdesign

Webbversionen är designad som en riktig bankomat, med interaktiv "hårdvara":

**Inget blink vid menybyten** (2026-09-27): kortremsan ("Sätt in ditt kort" + Visa/Mastercard) doldes förut av skriptet längst ner i sidan — efter att webbläsaren redan målat den en bildruta, vid varje menyval eftersom appen laddar om sidan. Ett synkront skript i `<head>` läser nu `sessionStorage` och sätter `bk-kort-isatt` på `<html>` före första bildrutan, och CSS:en döljer remsan innan den någonsin syns.

**Maskinen**
- **Mörk ATM-kropp** med glödande kortläsare, kvittoskrivare, statuslampa,
  borstad stålknappsats (med taktil punkt på 5:an) och uttagsfack ovanför knappsatsen
- **Knappsatsen fungerar** — siffrorna skriver i fokuserat fält, `*` ger decimalkomma,
  `#` är backsteg, RENSA tömmer, OK skickar formuläret och AVBRYT går till menyn
- **Statuslampan "I DRIFT"** pulserar grönt och blinkar gult "BEARBETAR" under transaktioner
- Hela maskinen skalas automatiskt så att den får plats i fönstrets höjd

**Introsekvens** (en gång per session, som en riktig Bankomat)
1. CRT power-on-svep och BIOS-självtest som skrivs ut rad för rad
2. "SÄTT IN DITT KORT" med kortikon och studsande pil
3. Kortet glider in i den glödande kortläsaren
4. "LÄSER KORT" → menyn tonas fram

**Animationer och effekter**
- **Uttagsanimation** — luckan öppnas, en sedelbunt matas upp ur uttagsfacket och tas
  (lösa sedlar fladdrar); på mobil scrollas uttagsfacket automatiskt fram
- **Kvitto skrivs ut** ur kvittofacket efter insättning/uttag/överföring och rivs av
- **Neongrön fosforskärm** med CRT-scanlines, fosformask, rullande refresh-band,
  vinjettering, subtilt flimmer och kromatisk aberration
- **Väggen bakom maskinen** (2026-09-29) — maskinen står mot en lugn betongvägg med plattskarvar, puts och en lampa ovanför. Över väggen spelar gatans ljus: en rosa neonskylt som andas och ibland flimrar, en turkos skylt och strålkastare från bilar som sveper förbi. Förut stod den i ett svart tomrum med grönt sifferregn; regnet finns kvar som en svag reflex i väggen
- Saldot räknas upp från 0, meddelanden glitchar in, felsidans text glitchar
- Alla effekter respekterar `prefers-reduced-motion`
- **Mobilanpassad** — sidoknappar och knappsats döljs på små skärmar

## Uppstartsskärm

Första besöket per flik visar en **vit, bankmässig** uppstartsskärm (`static/splash.js`) innan menyn. Den var förut svart med fosforgrönt sifferregn och är nu vit med marinblått, guldlinje och gröna bockar, som ett kontoutdrag. Raderna tickar in och bockas av:

| Rad | Visar | Källa |
|---|---|---|
| ☕ Java | Java- och Spring Boot-version | den körande JVM:en |
| 🐘 PostgreSQL | databasens namn och version + JDBC-drivrutin | `DatabaseMetaData` på anslutningen (`Systeminfo`) |
| 🧩 Teknikstack | Spring MVC · Thymeleaf · JdbcTemplate · Docker på Render | |
| 📊 Kontoregister | antal konton och kunder | registret |
| 💰 Förvaltat | summan av saldona i kr | registret |
| 🏦 · 💵 · 🔐 · 💳 | betaltjänst, sedelkassett, säkerhetsmodul, kortläsare | maskinens egen uppstart |

Foten visar **autodeploy via Render** med branch och commit (`RENDER_GIT_BRANCH`/`RENDER_GIT_COMMIT`). Inget är avskrivet: uppgraderas projektet till Java 28 eller en nyare PostgreSQL visar skärmen det av sig själv. Saknas en siffra visas raden utan den i stället för med en påhittad. CarAdvice nattrutin kontrollerar varje natt att uppgifterna stämmer (`scripts/splash-vakt.js` i CarAdvice). `?splash=1` tvingar fram skärmen.

## Arkitektur

Projektet är uppdelat i tre lager:

```
UI-lager          Meny, Val, Kontohantering, KontoversiktDialog, NyPersonForm,
                  NyttKontoForm, TransaktionDialog, PersonListDialog
    ↓
Servicelager      BankService — affärslogik och validering
    ↓
Datalager         BankRepository — SQL-frågor med PreparedStatement
                  KontoInfo, TransactionInfo — typade modeller (records)
```

- **UI-lagret** hanterar endast grafik och visar felmeddelanden via `JOptionPane`
- **BankService** validerar indata och kastar `BankException` med användarvänliga felmeddelanden
- **BankRepository** sköter all databaskommunikation med parametriserade frågor (skyddar mot SQL injection)
- Service- och datalagret har samma API i båda versionerna — `BankService` är identisk
  sånär som på Spring-annoteringarna, och `BankService` tar en injicerbar repository
  vilket gör logiken testbar med mockad databas
- Databasen skapas automatiskt om den inte finns; äldre databaser migreras automatiskt
  vid start (tidsstämpelkolumnen `created_at` och unika index läggs till)

## Kom igång

### Krav

- Java 27 (koden använder records)

### Kör med JAR (enklaste sättet)

JAR-filen innehåller allt — ingen separat JDBC-driver behövs:

```bash
java -jar min_labb3.jar
```

### Kompilera och kör från källkod

```bash
javac -cp "lib/sqlite-jdbc-3.7.15-M1.jar" -d out/production/min_labb3 src/main/java/bank/*.java
java -cp "out/production/min_labb3;lib/sqlite-jdbc-3.7.15-M1.jar" bank.Meny
```

### Bygga och testa med Maven

Skrivbordsversionen är även ett Maven-projekt (kräver Maven installerat):

```bash
mvn test        # kör skrivbordsversionens testsvit
```

## Projektstruktur

```
min_labb3/
├── web/                                   # Webbversion (Spring Boot)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/bank/                 # Controller, Service, Repository
│       │   └── resources/
│       │       ├── templates/             # Thymeleaf HTML-sidor
│       │       ├── static/style.css
│       │       └── application*.properties
│       └── test/java/bank/                # BankServiceTest, BankControllerTest
├── Dockerfile                             # Docker-bygge för Render
├── render.yaml                            # Render-konfiguration
├── pom.xml                                # Maven-bygge för skrivbordsversionen (tester)
├── src/                                   # Skrivbordsversion (Swing)
│   ├── test/
│   │   └── java/
│   │       └── bank/                      # BankServiceTest, BankRepositoryTest
│   └── main/
│       └── java/
│           └── bank/
│               ├── Meny.java              # Startpunkt — huvudmeny
│               ├── Val.java               # Undermeny för registervård
│               ├── Kontohantering.java    # Kontosökning och transaktioner
│               ├── KontoversiktDialog.java# Tabell över alla konton med totalsumma
│               ├── NyPersonForm.java      # Formulär för ny kontoinnehavare
│               ├── NyttKontoForm.java     # Formulär för nytt konto
│               ├── TransaktionDialog.java # Dialog för insättning, uttag och överföring
│               ├── PersonListDialog.java  # Lista, visa konton och ta bort kontoinnehavare
│               ├── UITheme.java           # Gemensam styling (färger, knappar, kort)
│               ├── KontoInfo.java         # Typad kontomodell (delas med webbversionen)
│               ├── TransactionInfo.java   # Typad transaktionsmodell med tidsstämpel
│               ├── BankService.java       # Affärslogik och validering
│               ├── BankRepository.java    # Databasåtkomst
│               └── BankException.java     # Felhantering mellan lagren
├── lib/
│   └── sqlite-jdbc-3.7.15-M1.jar         # SQLite JDBC-driver
├── min_labb3.jar                          # Körbar JAR (inkluderar allt)
├── werasbetal.sql                         # Databasschema och testdata
└── README.md
```

## Testdata (werasbetal.db)

Databasen innehåller Looney Tunes-karaktärer som testdata:

| Kontonummer | Typ | Innehavare | Saldo |
|-------------|-----|------------|-------|
| 121223 | Sparkonto | Sylvester | 16 000,50 kr |
| 12034500 | Lönekonto | Sylvester | 540,11 kr |
| 8264i33 | Sparkonto | Elmer Fudd | 1 000,50 kr |

## Regler och begränsningar

- Kontonummer måste vara minst 5 siffror
- Kontotyp måste vara `spar` eller `loen`
- Belopp måste vara större än 0 kr; maxbelopp per transaktion: 20 000 kr
- Uttag och överföringar kräver täckning på kontot (kontrolleras atomärt i databasen)
- Överföring till samma konto är inte tillåten
- Kontoinnehavaren måste finnas registrerad innan ett konto skapas
- En kontoinnehavare kan inte tas bort om aktiva konton finns
- Personnamn och kontonummer är unika (unika index i båda versionernas databaser)

## Tester

Båda versionerna har egna testsviter — totalt 61 tester (JUnit 5 + Mockito).
Alla körs automatiskt i CI vid varje push.

**Skrivbordsversionen** (38 tester):

- `BankServiceTest` — validering och affärslogik med mockad repository
  (belopp, kontonummer, decimalkomma, täckning m.m.)
- `BankRepositoryTest` — integrationstester mot en temporär SQLite-databas:
  riktiga SQL-frågor, rollback vid misslyckade uttag/överföringar,
  automatisk migrering av äldre databaser och de unika indexen

```bash
mvn test
```

**Webbversionen** (23 tester, + MockMvc):

- `BankServiceTest` — validering och affärslogik (belopp, kontonummer, täckning m.m.)
- `BankControllerTest` — sidrendering, inklusive regressionstest för Thymeleaf-mallarna

```bash
cd web
mvn test
```
## Upphovsrätt och användning

Copyright © 2026 Robert Andersson Kopler. Alla rättigheter förbehållna.

Koden är märkt med upphovsmannens namn i flera lager: som `@author` i varje Java-klass,
överst i varje serverad JS-, HTML- och PHP-fil, i `NOTICE`, i konstanten
`Authorship.AUTHOR` och i HTTP-huvudet `X-Author` på varje svar från tjänsten.
`Authorship` kontrollerar vid uppstart att konstanten inte ändrats och loggar ett fel om
den har det. Kontrollen stänger **aldrig** av tjänsten — en vakt som fäller en tjänst i
drift för att en textsträng ändrats gör mer skada än den förhindrar.

**Du får** läsa koden, köra den lokalt, lära av den och låta dig inspireras av den i egna
studie- och portföljprojekt.

**Du får inte** sprida den vidare som din egen, publicera kopior av den, eller använda den —
helt eller delvis — i kommersiellt syfte eller i en tjänst som konkurrerar med denna.

Vill du använda något härifrån utanför de ramarna går det ofta bra — fråga först.

Att ta bort märkningen ur källkoden är tekniskt möjligt för den som har koden. Det som
skyddar upphovet är upphovsrätten och git-historiken; lagren ovan finns för att göra ett
intrång arbetsamt och synligt, inte omöjligt.
