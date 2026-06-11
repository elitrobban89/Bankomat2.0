CREATE TABLE IF NOT EXISTS bank_person (
    name       VARCHAR(50),
    gatuadress VARCHAR(50),
    postnr     VARCHAR(5),
    stad       VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS bank_konto (
    kontonr   VARCHAR(13),
    kontotyp  VARCHAR(10),
    namn      VARCHAR(50),
    saldo     DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS bank_gjordatrans (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kontonr       VARCHAR(13),
    typ           VARCHAR(3),
    belopp        DOUBLE PRECISION,
    ocrmeddelande VARCHAR(70),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add created_at to existing databases that lack it
ALTER TABLE bank_gjordatrans ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
