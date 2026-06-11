CREATE TABLE IF NOT EXISTS person (
    name       VARCHAR(50),
    gatuadress VARCHAR(50),
    postnr     VARCHAR(5),
    stad       VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS konto (
    kontonr   VARCHAR(13),
    kontotyp  VARCHAR(10),
    namn      VARCHAR(50),
    saldo     DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS gjordatrans (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kontonr      VARCHAR(13),
    typ          VARCHAR(3),
    belopp       DOUBLE PRECISION,
    ocrmeddelande VARCHAR(70)
);
