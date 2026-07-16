# Bankomat 2.0

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
| Java-version | Java 17+ |

**Webbversion**

| Komponent | Teknologi |
|-----------|-----------|
| Backend | Spring Boot 3 |
| Templating | Thymeleaf |
| Databas | PostgreSQL |
| Hosting | Render (Docker) |
| Font | Share Tech Mono (Google Fonts) |

## Webbdesign

Webbversionen är designad som en riktig bankomat, med interaktiv "hårdvara":

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
- **Matrix-regn** och pulserande glöd bakom maskinen
- Saldot räknas upp från 0, meddelanden glitchar in, felsidans text glitchar
- Alla effekter respekterar `prefers-reduced-motion`
- **Mobilanpassad** — sidoknappar och knappsats döljs på små skärmar

## Arkitektur

Projektet är uppdelat i tre lager:

```
UI-lager          Meny, Val, Kontohantering, NyPersonForm, NyttKontoForm,
                  TransaktionDialog, PersonListDialog
    ↓
Servicelager      BankService — affärslogik och validering
    ↓
Datalager         BankRepository — SQL-frågor med PreparedStatement
```

- **UI-lagret** hanterar endast grafik och visar felmeddelanden via `JOptionPane`
- **BankService** validerar indata och kastar `BankException` med användarvänliga felmeddelanden
- **BankRepository** sköter all databaskommunikation med parametriserade frågor (skyddar mot SQL injection)
- Databasen skapas automatiskt om den inte finns

## Kom igång

### Krav

- Java 17 eller senare (koden använder records)

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
├── src/                                   # Skrivbordsversion (Swing)
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
- Personnamn och kontonummer är unika (unika index i webbversionens databas)

## Tester

Webbversionen har en testsvit med 23 tester (JUnit 5 + Mockito + MockMvc):

- `BankServiceTest` — validering och affärslogik (belopp, kontonummer, täckning m.m.)
- `BankControllerTest` — sidrendering, inklusive regressionstest för Thymeleaf-mallarna

```bash
cd web
mvn test
```
