package bank;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankRepository {

    private static final String DB_URL = "jdbc:sqlite:werasbetal.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection c = DriverManager.getConnection(DB_URL); Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS person (name VARCHAR(50), gatuadress VARCHAR(50), postnr CHAR(5), stad VARCHAR(50))");
                st.execute("CREATE TABLE IF NOT EXISTS konto (kontonr CHAR(13), kontotyp VARCHAR(10), namn VARCHAR(50), saldo DOUBLE)");
                st.execute("CREATE TABLE IF NOT EXISTS gjordatrans (kontonr CHAR(13), typ CHAR(3), belopp DOUBLE, OCRmeddelande VARCHAR(70))");
            }
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError("Kunde inte initiera databasen: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
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
                "INSERT INTO gjordatrans (kontonr, typ, belopp, OCRmeddelande) VALUES (?, ?, ?, ?)")) {
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

    public List<String> getKontoInfo(String kontonr) {
        String sql = "SELECT kontonr, kontotyp, namn, saldo FROM konto WHERE kontonr = ?";
        List<String> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String typ = "spar".equals(rs.getString(2)) ? "Sparkonto" : "Lönekonto";
                    result.add(String.format(
                        "Kontonummer:  %s%nTyp:          %s%nÄgare:        %s%nSaldo:        %.2f kr",
                        rs.getString(1), typ, rs.getString(3), rs.getDouble(4)));
                }
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }

    public List<String> getTransactions(String kontonr) {
        String sql = "SELECT typ, belopp, OCRmeddelande FROM gjordatrans WHERE kontonr = ?";
        List<String> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String typ = "ins".equals(rs.getString(1)) ? "Insättning" : "Uttag     ";
                    result.add(String.format("%-10s  %9.2f kr  –  %s",
                        typ, rs.getDouble(2), rs.getString(3)));
                }
            }
        } catch (SQLException e) { throw new BankException("Databasfel: " + e.getMessage()); }
        return result;
    }
}
