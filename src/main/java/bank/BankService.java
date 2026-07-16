package bank;

import java.util.List;

public class BankService {

    private static final double MAX_BELOPP = 20000;
    private final BankRepository repo;

    public BankService() {
        this(new BankRepository());
    }

    public BankService(BankRepository repo) {
        this.repo = repo;
    }

    public void newClient(String name, String gatuadress, String postnr, String stad) {
        if (name == null || name.trim().isEmpty())
            throw new BankException("Namnfältet får inte vara tomt");
        if (repo.personExists(name))
            throw new BankException("Namnet '" + name + "' är upptaget, försök igen");
        repo.insertPerson(name, gatuadress, postnr, stad);
    }

    public void newAccount(String kontonr, String kontotyp, String namn, String saldoStr) {
        if (kontonr == null || kontonr.trim().isEmpty())
            throw new BankException("Kontonummerfältet får inte vara tomt");
        if (kontotyp == null || kontotyp.trim().isEmpty())
            throw new BankException("Kontotypfältet får inte vara tomt");
        if (namn == null || namn.trim().isEmpty())
            throw new BankException("Namnfältet får inte vara tomt");
        validateAccountNumber(kontonr);
        if (repo.accountExists(kontonr))
            throw new BankException("Kontonumret är upptaget");
        validateAccountType(kontotyp);
        if (!repo.personExists(namn))
            throw new BankException("Namnet finns inte, försök igen");
        double saldo = parseSaldo(saldoStr);
        validateSaldo(saldo);
        repo.insertAccount(kontonr, kontotyp, namn, saldo);
    }

    public void deposit(String kontonr, String beloppStr, String ocr) {
        validateAccountNumber(kontonr);
        if (!repo.accountExists(kontonr))
            throw new BankException("Kontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        repo.deposit(kontonr, belopp, ocr);
    }

    public void withdraw(String kontonr, String beloppStr, String ocr) {
        validateAccountNumber(kontonr);
        if (!repo.accountExists(kontonr))
            throw new BankException("Kontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        repo.withdraw(kontonr, belopp, ocr);
    }

    public void transfer(String from, String to, String beloppStr, String ocr) {
        validateAccountNumber(from);
        validateAccountNumber(to);
        if (from.equals(to))
            throw new BankException("Det går inte att överföra till samma konto");
        if (!repo.accountExists(from))
            throw new BankException("Källkontonumret finns inte");
        if (!repo.accountExists(to))
            throw new BankException("Målkontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        repo.transfer(from, to, belopp, ocr);
    }

    public KontoInfo getKontoDetails(String kontonr) {
        validateAccountNumber(kontonr);
        KontoInfo info = repo.getKontoDetails(kontonr);
        if (info == null)
            throw new BankException("Felaktigt kontonummer");
        return info;
    }

    public List<KontoInfo> getAccountsByPerson(String namn) {
        return repo.getAccountsByPerson(namn);
    }

    public List<KontoInfo> getAllAccounts() {
        return repo.getAllAccounts();
    }

    public List<TransactionInfo> getTransactions(String kontonr) {
        return repo.getTransactions(kontonr);
    }

    public double getTotalSaldo(String namn) {
        return repo.getTotalSaldo(namn);
    }

    public List<String> getAllPersonNames() {
        return repo.getAllPersonNames();
    }

    public void deletePerson(String name) {
        if (repo.personHasAccounts(name))
            throw new BankException("Kontoinnehavaren har aktiva konton — ta bort dessa först");
        repo.deletePerson(name);
    }

    public void deleteAccount(String kontonr) {
        if (!repo.accountExists(kontonr))
            throw new BankException("Kontot finns inte");
        repo.deleteAccount(kontonr);
    }

    private void validateAccountNumber(String kontonr) {
        if (kontonr == null || kontonr.length() < 5 || !kontonr.chars().allMatch(Character::isDigit))
            throw new BankException("Felaktigt kontonr, försök igen (minst 5 siffror)");
    }

    private void validateAccountType(String typ) {
        if (!"spar".equals(typ) && !"loen".equals(typ))
            throw new BankException("Fel kontotyp, ange spar eller loen");
    }

    private void validateBelopp(double belopp) {
        if (belopp <= 0)
            throw new BankException("Beloppet måste vara större än 0 kr");
        if (belopp > MAX_BELOPP)
            throw new BankException("Galet belopp. Maxbelopp 20 000 kr");
    }

    private void validateSaldo(double saldo) {
        if (saldo < 0)
            throw new BankException("Saldot får inte vara negativt");
        if (saldo > MAX_BELOPP)
            throw new BankException("Galet belopp. Maxbelopp 20 000 kr");
    }

    private double parseBelopp(String str) {
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new BankException("Felaktigt belopp");
        }
    }

    private double parseSaldo(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
