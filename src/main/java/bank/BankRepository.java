package bank;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BankRepository {

    private static final String DEFAULT_DB_URL = "jdbc:sqlite:werasbetal.db";
    private static final DateTimeFormatter SQLITE_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String dbUrl;

    public BankRepository() {
        this(DEFAULT_DB_URL);
    }

    /** Databas-URL:en är konfigurerbar så att tester kan köra mot en temporär databas. */
    public BankRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection c = DriverManager.getConnection(dbUrl); Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS person (name VARCHAR(50), gatuadress VARCHAR(50), postnr CHAR(5), stad VARCHAR(50))");
                st.execute("CREATE TABLE IF NOT EXISTS konto (kontonr CHAR(13), kontotyp VARCHAR(10), namn VARCHAR(50), saldo DOUBLE)");
                st.execute("CREATE TABLE IF NOT EXISTS gjordatrans (kontonr CHAR(13), typ CHAR(3), belopp DOUBLE, OCRmeddelande VARCHAR(70), created_at TIMESTAMP)");
                addCreatedAtIfMissing(c, st);
                createUniqueIndexes(st);
            }
        } catch (ClassNotFoundException e) {
            throw new BankException("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            throw new BankException("Kunde inte initiera databasen: " + e.getMessage());
        }
    }

    /** Lägger till created_at i befintliga databaser som saknar kolumnen. */
    private static void addCreatedAtIfMissing(Connection c, Statement st) throws SQLException {
        boolean hasCreatedAt = false;
        try (ResultSet rs = st.executeQuery("PRAGMA table_info(gjordatrans)")) {
            while (rs.next()) {
                if ("created_at".equalsIgnoreCase(rs.getString("name"))) {
                    hasCreatedAt = true;
                    break;
                }
            }
        }
        if (!hasCreatedAt) {
            try (Statement alter = c.createStatement()) {
                alter.execute("ALTER TABLE gjordatrans ADD COLUMN created_at TIMESTAMP");
            }
        }
    }

    /** Samma unika index som webbversionens schema. Misslyckas skapandet
     *  (t.ex. gamla dubbletter i databasen) fortsätter appen utan index. */
    private static void createUniqueIndexes(Statement st) {
        try {
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_person_name ON person (name)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_konto_kontonr ON konto (kontonr)");
        } catch (SQLException e) {
            System.err.println("Varning: kunde inte skapa unika index: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public boolean personExists(String name) {
        String sql = "SELECT 1 FROM person WHERE name = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public boolean accountExists(String kontonr) {
        String sql = "SELECT 1 FROM konto WHERE kontonr = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public boolean personHasAccounts(String name) {
        String sql = "SELECT 1 FROM konto WHERE namn = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void insertPerson(String name, String gatuadress, String postnr, String stad) {
        String sql = "INSERT INTO person (name, gatuadress, postnr, stad) VALUES (?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, gatuadress);
            ps.setString(3, postnr); ps.setString(4, stad);
            ps.executeUpdate();
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void insertAccount(String kontonr, String kontotyp, String namn, double saldo) {
        String sql = "INSERT INTO konto (kontonr, kontotyp, namn, saldo) VALUES (?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr); ps.setString(2, kontotyp);
            ps.setString(3, namn); ps.setDouble(4, saldo);
            ps.executeUpdate();
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void deposit(String kontonr, double belopp, String ocr) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                addToSaldo(c, kontonr, belopp);
                insertTransaction(c, kontonr, "ins", belopp, ocr);
                c.commit();
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void withdraw(String kontonr, double belopp, String ocr) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                subtractIfSufficient(c, kontonr, belopp);
                insertTransaction(c, kontonr, "utt", belopp, ocr);
                c.commit();
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void transfer(String from, String to, double belopp, String ocr) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                subtractIfSufficient(c, from, belopp);
                addToSaldo(c, to, belopp);
                insertTransaction(c, from, "utt", belopp, ocr);
                insertTransaction(c, to, "ins", belopp, ocr);
                c.commit();
            } catch (SQLException | BankException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    private void addToSaldo(Connection c, String kontonr, double belopp) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE konto SET saldo = saldo + ? WHERE kontonr = ?")) {
            ps.setDouble(1, belopp); ps.setString(2, kontonr);
            ps.executeUpdate();
        }
    }

    private void subtractIfSufficient(Connection c, String kontonr, double belopp) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE konto SET saldo = saldo - ? WHERE kontonr = ? AND saldo >= ?")) {
            ps.setDouble(1, belopp); ps.setString(2, kontonr); ps.setDouble(3, belopp);
            if (ps.executeUpdate() == 0)
                throw new BankException("Inte tillräckligt med pengar på kontot");
        }
    }

    private void insertTransaction(Connection c, String kontonr, String typ, double belopp, String ocr) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO gjordatrans (kontonr, typ, belopp, OCRmeddelande, created_at) " +
                "VALUES (?, ?, ?, ?, datetime('now', 'localtime'))")) {
            ps.setString(1, kontonr); ps.setString(2, typ);
            ps.setDouble(3, belopp); ps.setString(4, ocr);
            ps.executeUpdate();
        }
    }

    public void deleteAccount(String kontonr) {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM gjordatrans WHERE kontonr = ?")) {
                    ps.setString(1, kontonr); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM konto WHERE kontonr = ?")) {
                    ps.setString(1, kontonr); ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public void deletePerson(String name) {
        String sql = "DELETE FROM person WHERE name = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.executeUpdate();
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public List<String> getAllPersonNames() {
        String sql = "SELECT name FROM person ORDER BY name";
        List<String> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString(1));
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }

    public KontoInfo getKontoDetails(String kontonr) {
        String sql = "SELECT kontonr, kontotyp, namn, saldo FROM konto WHERE kontonr = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return readKonto(rs);
                return null;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public List<KontoInfo> getAccountsByPerson(String namn) {
        String sql = "SELECT kontonr, kontotyp, namn, saldo FROM konto WHERE namn = ? ORDER BY kontonr";
        List<KontoInfo> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, namn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(readKonto(rs));
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }

    public List<KontoInfo> getAllAccounts() {
        String sql = "SELECT kontonr, kontotyp, namn, saldo FROM konto ORDER BY namn, kontonr";
        List<KontoInfo> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(readKonto(rs));
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }

    public double getTotalSaldo(String namn) {
        String sql = "SELECT COALESCE(SUM(saldo), 0) FROM konto WHERE namn = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, namn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
    }

    public List<TransactionInfo> getTransactions(String kontonr) {
        String sql = "SELECT typ, belopp, OCRmeddelande, created_at FROM gjordatrans WHERE kontonr = ? ORDER BY rowid DESC";
        List<TransactionInfo> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new TransactionInfo(
                        rs.getString(1), rs.getDouble(2), rs.getString(3),
                        parseTimestamp(rs.getString(4))));
                }
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }

    private static KontoInfo readKonto(ResultSet rs) throws SQLException {
        return new KontoInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4));
    }

    /** Transaktioner från före uppgraderingen saknar tidsstämpel — då returneras null. */
    private static LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, SQLITE_TS);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }
}
