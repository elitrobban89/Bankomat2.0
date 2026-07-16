package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Integrationstester mot en riktig (temporär) SQLite-databas. */
class BankRepositoryTest {

    @TempDir
    Path tempDir;

    private String dbUrl;
    private BankRepository repo;

    @BeforeEach
    void setUp() {
        dbUrl = "jdbc:sqlite:" + tempDir.resolve("test.db");
        repo = new BankRepository(dbUrl);
    }

    private void skapaKonto(String kontonr, double saldo) {
        if (!repo.personExists("Anna")) repo.insertPerson("Anna", "Gatan 1", "12345", "Stan");
        repo.insertAccount(kontonr, "spar", "Anna", saldo);
    }

    // ─── Schema och personer ──────────────────────────────────────────────────

    @Test
    void skapaOchHittaPerson() {
        assertFalse(repo.personExists("Anna"));
        repo.insertPerson("Anna", "Gatan 1", "12345", "Stan");
        assertTrue(repo.personExists("Anna"));
        assertEquals(List.of("Anna"), repo.getAllPersonNames());
    }

    @Test
    void unikaIndexStopparDubblettPerson() {
        repo.insertPerson("Anna", "", "", "");
        assertThrows(BankException.class, () -> repo.insertPerson("Anna", "", "", ""));
    }

    @Test
    void unikaIndexStopparDubblettKontonummer() {
        skapaKonto("12345", 100);
        assertThrows(BankException.class, () -> repo.insertAccount("12345", "loen", "Anna", 0));
    }

    // ─── Konton ───────────────────────────────────────────────────────────────

    @Test
    void getKontoDetailsReturnerarKonto() {
        skapaKonto("12345", 250.50);
        KontoInfo info = repo.getKontoDetails("12345");
        assertNotNull(info);
        assertEquals("12345", info.kontonr());
        assertEquals("Anna", info.namn());
        assertEquals(250.50, info.saldo(), 0.001);
    }

    @Test
    void getKontoDetailsReturnerarNullForOkantKonto() {
        assertNull(repo.getKontoDetails("99999"));
    }

    @Test
    void getAllAccountsOchAccountsByPerson() {
        skapaKonto("11111", 100);
        skapaKonto("22222", 200);
        repo.insertPerson("Bertil", "", "", "");
        repo.insertAccount("33333", "loen", "Bertil", 300);

        assertEquals(3, repo.getAllAccounts().size());
        assertEquals(2, repo.getAccountsByPerson("Anna").size());
        assertEquals(300.0, repo.getTotalSaldo("Anna"), 0.001);
        assertEquals(0.0, repo.getTotalSaldo("Okänd"), 0.001);
    }

    // ─── Transaktioner ────────────────────────────────────────────────────────

    @Test
    void depositUppdaterarSaldoOchLoggarMedTidsstampel() {
        skapaKonto("12345", 100);
        repo.deposit("12345", 50, "OCR");

        assertEquals(150.0, repo.getKontoDetails("12345").saldo(), 0.001);
        List<TransactionInfo> trans = repo.getTransactions("12345");
        assertEquals(1, trans.size());
        assertEquals("ins", trans.get(0).typ());
        assertNotNull(trans.get(0).createdAt(), "Nya transaktioner ska ha tidsstämpel");
    }

    @Test
    void withdrawMedTackningDrasFranSaldot() {
        skapaKonto("12345", 100);
        repo.withdraw("12345", 60, "");
        assertEquals(40.0, repo.getKontoDetails("12345").saldo(), 0.001);
    }

    @Test
    void withdrawUtanTackningRullasTillbaka() {
        skapaKonto("12345", 100);
        assertThrows(BankException.class, () -> repo.withdraw("12345", 500, ""));
        assertEquals(100.0, repo.getKontoDetails("12345").saldo(), 0.001);
        assertTrue(repo.getTransactions("12345").isEmpty(), "Misslyckat uttag ska inte loggas");
    }

    @Test
    void transferFlyttarPengarOchLoggarBadaKontona() {
        skapaKonto("11111", 500);
        skapaKonto("22222", 0);
        repo.transfer("11111", "22222", 200, "hyra");

        assertEquals(300.0, repo.getKontoDetails("11111").saldo(), 0.001);
        assertEquals(200.0, repo.getKontoDetails("22222").saldo(), 0.001);
        assertEquals("utt", repo.getTransactions("11111").get(0).typ());
        assertEquals("ins", repo.getTransactions("22222").get(0).typ());
    }

    @Test
    void transferUtanTackningRullasTillbaka() {
        skapaKonto("11111", 100);
        skapaKonto("22222", 0);
        assertThrows(BankException.class, () -> repo.transfer("11111", "22222", 500, ""));
        assertEquals(100.0, repo.getKontoDetails("11111").saldo(), 0.001);
        assertEquals(0.0, repo.getKontoDetails("22222").saldo(), 0.001);
    }

    @Test
    void deleteAccountTarBortKontoOchTransaktioner() {
        skapaKonto("12345", 100);
        repo.deposit("12345", 50, "");
        repo.deleteAccount("12345");

        assertFalse(repo.accountExists("12345"));
        assertTrue(repo.getTransactions("12345").isEmpty());
    }

    // ─── Migrering av äldre databas ───────────────────────────────────────────

    @Test
    void aldreDatabasFarCreatedAtKolumnenVidStart() throws Exception {
        String oldUrl = "jdbc:sqlite:" + tempDir.resolve("old.db");
        try (Connection c = DriverManager.getConnection(oldUrl); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE person (name VARCHAR(50), gatuadress VARCHAR(50), postnr CHAR(5), stad VARCHAR(50))");
            st.execute("CREATE TABLE konto (kontonr CHAR(13), kontotyp VARCHAR(10), namn VARCHAR(50), saldo DOUBLE)");
            st.execute("CREATE TABLE gjordatrans (kontonr CHAR(13), typ CHAR(3), belopp DOUBLE, OCRmeddelande VARCHAR(70))");
            st.execute("INSERT INTO person VALUES ('Anna', '', '', '')");
            st.execute("INSERT INTO konto VALUES ('12345', 'spar', 'Anna', 100)");
            st.execute("INSERT INTO gjordatrans VALUES ('12345', 'ins', 100, 'gammal rad')");
        }

        BankRepository migrerad = new BankRepository(oldUrl);

        List<TransactionInfo> trans = migrerad.getTransactions("12345");
        assertEquals(1, trans.size());
        assertNull(trans.get(0).createdAt(), "Gamla rader saknar tidsstämpel");
        assertEquals("–", trans.get(0).datumDisplay());

        migrerad.deposit("12345", 50, "ny rad");
        assertNotNull(migrerad.getTransactions("12345").get(0).createdAt(),
                "Nya rader i migrerad databas ska ha tidsstämpel");
    }
}
