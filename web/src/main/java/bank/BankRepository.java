package bank;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BankRepository {

    private final JdbcTemplate jdbc;

    public BankRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean personExists(String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM person WHERE name = ?", Integer.class, name);
        return n != null && n > 0;
    }

    public boolean accountExists(String kontonr) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM konto WHERE kontonr = ?", Integer.class, kontonr);
        return n != null && n > 0;
    }

    public boolean personHasAccounts(String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM konto WHERE namn = ?", Integer.class, name);
        return n != null && n > 0;
    }

    public void insertPerson(String name, String gatuadress, String postnr, String stad) {
        jdbc.update("INSERT INTO person (name, gatuadress, postnr, stad) VALUES (?, ?, ?, ?)",
                name, gatuadress, postnr, stad);
    }

    public void insertAccount(String kontonr, String kontotyp, String namn, double saldo) {
        jdbc.update("INSERT INTO konto (kontonr, kontotyp, namn, saldo) VALUES (?, ?, ?, ?)",
                kontonr, kontotyp, namn, saldo);
    }

    public double getSaldo(String kontonr) {
        Double saldo = jdbc.queryForObject("SELECT saldo FROM konto WHERE kontonr = ?", Double.class, kontonr);
        if (saldo == null) throw new BankException("Kontonummer finns inte!");
        return saldo;
    }

    public void updateSaldo(String kontonr, double saldo) {
        jdbc.update("UPDATE konto SET saldo = ? WHERE kontonr = ?", saldo, kontonr);
    }

    public void insertTransaction(String kontonr, String typ, double belopp, String ocr) {
        jdbc.update("INSERT INTO gjordatrans (kontonr, typ, belopp, ocrmeddelande) VALUES (?, ?, ?, ?)",
                kontonr, typ, belopp, ocr);
    }

    public void deleteAccount(String kontonr) {
        jdbc.update("DELETE FROM gjordatrans WHERE kontonr = ?", kontonr);
        jdbc.update("DELETE FROM konto WHERE kontonr = ?", kontonr);
    }

    public void deletePerson(String name) {
        jdbc.update("DELETE FROM person WHERE name = ?", name);
    }

    public List<String> getAllPersonNames() {
        return jdbc.queryForList("SELECT name FROM person ORDER BY name", String.class);
    }

    public KontoInfo getKontoDetails(String kontonr) {
        List<KontoInfo> result = jdbc.query(
                "SELECT kontonr, kontotyp, namn, saldo FROM konto WHERE kontonr = ?",
                (rs, i) -> new KontoInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4)),
                kontonr);
        return result.isEmpty() ? null : result.get(0);
    }

    public List<String> getTransactions(String kontonr) {
        return jdbc.query(
                "SELECT typ, belopp, ocrmeddelande FROM gjordatrans WHERE kontonr = ? ORDER BY id DESC",
                (rs, i) -> {
                    String typLabel = "ins".equals(rs.getString(1)) ? "Insättning" : "Uttag";
                    String ocr = rs.getString(3);
                    String ocrPart = (ocr != null && !ocr.isBlank()) ? "  –  " + ocr : "";
                    return String.format("%-10s  %,.2f kr%s", typLabel, rs.getDouble(2), ocrPart);
                },
                kontonr);
    }
}
