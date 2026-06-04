# Bankomat 2.0

Ett internt bankhanteringssystem byggt med Java Swing och SQLite. Applikationen hanterar kontoinnehavare, konton och transaktioner via ett grafiskt gränssnitt.

## Funktioner

- Skapa nya kontoinnehavare
- Skapa nya konton (sparkonto eller lönekonto)
- Sätta in och ta ut pengar
- Överföra pengar mellan konton
- Söka upp kontoinformation och transaktionshistorik

## Teknisk stack

| Komponent | Teknologi |
|-----------|-----------|
| GUI | Java Swing |
| Databas | SQLite |
| JDBC-driver | sqlite-jdbc 3.7.15 |
| Java-version | Java 8+ |

## Arkitektur

Projektet är uppdelat i tre lager:

```
UI-lager          Meny, Val, Kontohantering, NewJFrame3
    ↓
Servicelager      BankService — affärslogik och validering
    ↓
Datalager         BankRepository — SQL-frågor med PreparedStatement
```

- **UI-lagret** hanterar endast grafik och visar felmeddelanden via `JOptionPane`
- **BankService** validerar indata och kastar `BankException` med användarvänliga felmeddelanden
- **BankRepository** sköter all databaskommunikation med parametriserade frågor (skyddar mot SQL injection)

## Kom igång

### Krav

- Java 8 eller senare
- `lib/sqlite-jdbc-3.7.15-M1.jar` (ingår i repot)
- Databasfilen `werasbetal.db` (ingår i repot)

### Kompilera och kör

```bash
javac -cp "lib/sqlite-jdbc-3.7.15-M1.jar" -d out/production/min_labb3 src/main/java/bank/*.java
java -cp "out/production/min_labb3;lib/sqlite-jdbc-3.7.15-M1.jar" bank.Meny
```

### Kör med JAR

```bash
java -jar min_labb3.jar
```

## Projektstruktur

```
min_labb3/
├── src/
│   └── main/
│       └── java/
│           └── bank/
│               ├── Meny.java              # Startpunkt — huvudmeny
│               ├── Val.java               # Undermeny för registervård
│               ├── Kontohantering.java    # Kontosökning
│               ├── NewJFrame3.java        # Formulär för nytt konto
│               ├── UITheme.java           # Gemensam styling (färger, knappar, kort)
│               ├── BankService.java       # Affärslogik och validering
│               ├── BankRepository.java    # Databasåtkomst
│               └── BankException.java     # Felhantering mellan lagren
├── lib/
│   └── sqlite-jdbc-3.7.15-M1.jar         # SQLite JDBC-driver
├── out/
│   └── production/min_labb3/             # Kompilerade .class-filer
├── werasbetal.db                          # SQLite-databas
├── werasbetal.sql                         # Databasschema och testdata
└── README.md
```

## Regler och begränsningar

- Kontonummer måste vara minst 5 siffror
- Kontotyp måste vara `spar` eller `loen`
- Maxbelopp per transaktion: 20 000 kr
- Kontoinnehavaren måste finnas registrerad innan ett konto skapas
