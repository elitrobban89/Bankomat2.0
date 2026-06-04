package bank;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankRepository {

    private static final String DB_URL = "jdbc:sqlite:werasbetal.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public boolean personExists(String name) {
        String sql = "SELECT 1 FROM person WHERE name = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public boolean accountExists(String kontonr) {
        String sql = "SELECT 1 FROM konto WHERE kontonr = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public void insertPerson(String name, String gatuadress, String postnr, String stad) {
        String sql = "INSERT INTO person (name, gatuadress, postnr, stad) VALUES (?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, gatuadress);
            ps.setString(3, postnr);
            ps.setString(4, stad);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public void insertAccount(String kontonr, String kontotyp, String namn, double saldo) {
        String sql = "INSERT INTO konto (kontonr, kontotyp, namn, saldo) VALUES (?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            ps.setString(2, kontotyp);
            ps.setString(3, namn);
            ps.setDouble(4, saldo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public double getSaldo(String kontonr) {
        String sql = "SELECT saldo FROM konto WHERE kontonr = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
                throw new BankException("Kontonummer finns inte!");
            }
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public void updateSaldo(String kontonr, double saldo) {
        String sql = "UPDATE konto SET saldo = ? WHERE kontonr = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, saldo);
            ps.setString(2, kontonr);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public void insertTransaction(String kontonr, String typ, double belopp, String ocr) {
        String sql = "INSERT INTO gjordatrans (kontonr, typ, belopp, OCRmeddelande) VALUES (?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            ps.setString(2, typ);
            ps.setDouble(3, belopp);
            ps.setString(4, ocr);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
    }

    public List<String> getKontoInfo(String kontonr) {
        String sql = "SELECT kontonr, kontotyp, namn, saldo FROM konto WHERE kontonr = ?";
        List<String> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add("Kontonr: " + rs.getString(1)
                            + ", Typ: " + rs.getString(2)
                            + ", Namn: " + rs.getString(3)
                            + ", Saldo: " + rs.getString(4));
                }
            }
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
        return result;
    }

    public List<String> getTransactions(String kontonr) {
        String sql = "SELECT kontonr, typ, belopp, OCRmeddelande FROM gjordatrans WHERE kontonr = ?";
        List<String> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kontonr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add("Kontonr: " + rs.getString(1)
                            + ", Typ: " + rs.getString(2)
                            + ", Belopp: " + rs.getString(3)
                            + ", OCR: " + rs.getString(4));
                }
            }
        } catch (SQLException e) {
            throw new BankException("Databasfel: " + e.getMessage());
        }
        return result;
    }
}
