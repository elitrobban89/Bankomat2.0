package test;

import java.util.List;

public class BankService {

    private static final double MAX_BELOPP = 20000;
    private final BankRepository repo;

    public BankService() {
        this.repo = new BankRepository();
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
        validateBelopp(saldo);
        repo.insertAccount(kontonr, kontotyp, namn, saldo);
    }

    public void deposit(String kontonr, String beloppStr, String ocr) {
        validateAccountNumber(kontonr);
        if (!repo.accountExists(kontonr))
            throw new BankException("Kontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        repo.updateSaldo(kontonr, repo.getSaldo(kontonr) + belopp);
        repo.insertTransaction(kontonr, "ins", belopp, ocr);
    }

    public void withdraw(String kontonr, String beloppStr, String ocr) {
        validateAccountNumber(kontonr);
        if (!repo.accountExists(kontonr))
            throw new BankException("Kontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        double saldo = repo.getSaldo(kontonr);
        if (saldo < belopp)
            throw new BankException("Inte tillräckligt med pengar på kontot");
        repo.updateSaldo(kontonr, saldo - belopp);
        repo.insertTransaction(kontonr, "utt", belopp, ocr);
    }

    public void transfer(String from, String to, String beloppStr, String ocr) {
        validateAccountNumber(from);
        validateAccountNumber(to);
        if (!repo.accountExists(from))
            throw new BankException("Källkontonumret finns inte");
        if (!repo.accountExists(to))
            throw new BankException("Målkontonumret finns inte");
        double belopp = parseBelopp(beloppStr);
        validateBelopp(belopp);
        double saldo = repo.getSaldo(from);
        if (saldo < belopp)
            throw new BankException("Inte tillräckligt med pengar på kontot");
        repo.updateSaldo(from, saldo - belopp);
        repo.updateSaldo(to, repo.getSaldo(to) + belopp);
        repo.insertTransaction(from, "utt", belopp, ocr);
        repo.insertTransaction(to, "ins", belopp, ocr);
    }

    public List<String> getKontoInfo(String kontonr) {
        List<String> result = repo.getKontoInfo(kontonr);
        if (result.isEmpty())
            throw new BankException("Felaktigt kontonummer");
        return result;
    }

    public List<String> getTransactions(String kontonr) {
        return repo.getTransactions(kontonr);
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
        if (belopp > MAX_BELOPP)
            throw new BankException("Galet belopp. Maxbelopp 20 000 kr");
    }

    private double parseBelopp(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new BankException("Felaktigt belopp");
        }
    }

    private double parseSaldo(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
