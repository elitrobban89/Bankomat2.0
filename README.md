# Bankomat 2.0

Ett internt bankhanteringssystem med två versioner:

- **Webbversion (live):** [elitrobban.se/bankomat-2-0/](https://elitrobban.se/bankomat-2-0/) — Spring Boot + Thymeleaf + PostgreSQL, driftsatt på Render
- **Skrivbordsversion:** Java Swing + SQLite, körs lokalt

## Funktioner

**Registervård**
- Registrera nya kontoinnehavare
- Skapa nya konton (sparkonto eller lönekonto)
- Visa lista över alla kontoinnehavare
- Ta bort kontoinnehavare (kräver att inga aktiva konton finns)

**Kontohantering**
- Söka upp konto med kontonummer
- Sätta in pengar
- Ta ut pengar
- Överföra pengar mellan konton
- Visa scrollbar transaktionshistorik
- Saldo uppdateras automatiskt efter varje transaktion
- Ta bort konto (alla tillhörande transaktioner tas också bort)

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
| Java-version | Java 8+ |

**Webbversion**

| Komponent | Teknologi |
|-----------|-----------|
| Backend | Spring Boot 3 |
| Templating | Thymeleaf |
| Databas | PostgreSQL |
| Hosting | Render (Docker) |
| Font | Share Tech Mono (Google Fonts) |

## Webbdesign

Webbversionen är designad som en riktig bankomat:

- **Mörk ATM-kropp** med kortläsare, sifferblock och uttagsfack
- **Neongrön fosforskärm** med CRT-scanlines och glow-effekter
- **Terminal-typsnitt** (Share Tech Mono) för autentisk känsla
- **Flimmeranimation** på loggan och pulserande hover-effekter
- **Laddningsindikator** visas vid alla formulärinskickningar
- **Mobilanpassad** — sidobchottar och knappsats döljs på små skärmar

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

- Java 8 eller senare

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
│   └── src/main/
│       ├── java/bank/                     # Controller, Service, Repository
│       └── resources/
│           ├── templates/                 # Thymeleaf HTML-sidor
│           ├── static/style.css
│           └── application*.properties
├── Dockerfile                             # Docker-bygge för Render
├── render.yaml                            # Render-konfiguration
├── src/                                   # Skrivbordsversion (Swing)
│   └── main/
│       └── java/
│           └── bank/
│               ├── Meny.java              # Startpunkt — huvudmeny
│               ├── Val.java               # Undermeny för registervård
│               ├── Kontohantering.java    # Kontosökning och transaktioner
│               ├── NyPersonForm.java      # Formulär för ny kontoinnehavare
│               ├── NyttKontoForm.java     # Formulär för nytt konto
│               ├── TransaktionDialog.java # Dialog för insättning, uttag och överföring
│               ├── PersonListDialog.java  # Lista och ta bort kontoinnehavare
│               ├── UITheme.java           # Gemensam styling (färger, knappar, kort)
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
- Maxbelopp per transaktion: 20 000 kr
- Kontoinnehavaren måste finnas registrerad innan ett konto skapas
- En kontoinnehavare kan inte tas bort om aktiva konton finns
