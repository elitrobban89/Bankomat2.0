package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankServiceTest {

    private BankRepository repo;
    private BankService service;

    @BeforeEach
    void setUp() {
        repo = mock(BankRepository.class);
        service = new BankService(repo);
    }

    // ─── Insättning ───────────────────────────────────────────────────────────

    @Test
    void depositAnroparRepositoryMedTolkatBelopp() {
        when(repo.accountExists("12345")).thenReturn(true);
        service.deposit("12345", "500", "OCR1");
        verify(repo).deposit("12345", 500.0, "OCR1");
    }

    @Test
    void depositTolkarDecimalkomma() {
        when(repo.accountExists("12345")).thenReturn(true);
        service.deposit("12345", "100,50", "");
        verify(repo).deposit("12345", 100.50, "");
    }

    @Test
    void depositAvvisarNegativtBelopp() {
        when(repo.accountExists("12345")).thenReturn(true);
        BankException e = assertThrows(BankException.class,
                () -> service.deposit("12345", "-500", ""));
        assertEquals("Beloppet måste vara större än 0 kr", e.getMessage());
        verify(repo, never()).deposit(anyString(), anyDouble(), anyString());
    }

    @Test
    void depositAvvisarNollbelopp() {
        when(repo.accountExists("12345")).thenReturn(true);
        assertThrows(BankException.class, () -> service.deposit("12345", "0", ""));
        verify(repo, never()).deposit(anyString(), anyDouble(), anyString());
    }

    @Test
    void depositAvvisarBeloppOverMax() {
        when(repo.accountExists("12345")).thenReturn(true);
        assertThrows(BankException.class, () -> service.deposit("12345", "20001", ""));
    }

    @Test
    void depositAvvisarOkantKonto() {
        when(repo.accountExists("12345")).thenReturn(false);
        assertThrows(BankException.class, () -> service.deposit("12345", "500", ""));
    }

    @Test
    void depositAvvisarOgiltigtBelopp() {
        when(repo.accountExists("12345")).thenReturn(true);
        assertThrows(BankException.class, () -> service.deposit("12345", "abc", ""));
    }

    @Test
    void depositAvvisarForKortKontonummer() {
        assertThrows(BankException.class, () -> service.deposit("123", "500", ""));
        verify(repo, never()).deposit(anyString(), anyDouble(), anyString());
    }

    // ─── Uttag ────────────────────────────────────────────────────────────────

    @Test
    void withdrawAnroparRepository() {
        when(repo.accountExists("12345")).thenReturn(true);
        service.withdraw("12345", "200", "OCR2");
        verify(repo).withdraw("12345", 200.0, "OCR2");
    }

    @Test
    void withdrawAvvisarNegativtBelopp() {
        when(repo.accountExists("12345")).thenReturn(true);
        assertThrows(BankException.class, () -> service.withdraw("12345", "-500", ""));
        verify(repo, never()).withdraw(anyString(), anyDouble(), anyString());
    }

    @Test
    void withdrawSlapperIgenomRepositoryFel() {
        when(repo.accountExists("12345")).thenReturn(true);
        doThrow(new BankException("Inte tillräckligt med pengar på kontot"))
                .when(repo).withdraw("12345", 500.0, "");
        BankException e = assertThrows(BankException.class,
                () -> service.withdraw("12345", "500", ""));
        assertEquals("Inte tillräckligt med pengar på kontot", e.getMessage());
    }

    // ─── Överföring ───────────────────────────────────────────────────────────

    @Test
    void transferAnroparRepository() {
        when(repo.accountExists("11111")).thenReturn(true);
        when(repo.accountExists("22222")).thenReturn(true);
        service.transfer("11111", "22222", "300", "OCR3");
        verify(repo).transfer("11111", "22222", 300.0, "OCR3");
    }

    @Test
    void transferAvvisarSammaKonto() {
        assertThrows(BankException.class,
                () -> service.transfer("11111", "11111", "300", ""));
        verify(repo, never()).transfer(anyString(), anyString(), anyDouble(), anyString());
    }

    @Test
    void transferAvvisarOkantMalkonto() {
        when(repo.accountExists("11111")).thenReturn(true);
        when(repo.accountExists("22222")).thenReturn(false);
        assertThrows(BankException.class,
                () -> service.transfer("11111", "22222", "300", ""));
        verify(repo, never()).transfer(anyString(), anyString(), anyDouble(), anyString());
    }

    // ─── Kontodetaljer ────────────────────────────────────────────────────────

    @Test
    void getKontoDetailsReturnerarKonto() {
        when(repo.getKontoDetails("12345")).thenReturn(new KontoInfo("12345", "spar", "Anna", 100.0));
        KontoInfo info = service.getKontoDetails("12345");
        assertEquals("Sparkonto", info.kontotypDisplay());
    }

    @Test
    void getKontoDetailsAvvisarOkantKonto() {
        when(repo.getKontoDetails("12345")).thenReturn(null);
        assertThrows(BankException.class, () -> service.getKontoDetails("12345"));
    }

    @Test
    void getKontoDetailsAvvisarOgiltigtKontonummer() {
        assertThrows(BankException.class, () -> service.getKontoDetails("abc"));
    }

    // ─── Ny kontoinnehavare ───────────────────────────────────────────────────

    @Test
    void newClientAvvisarTomtNamn() {
        assertThrows(BankException.class, () -> service.newClient("  ", "", "", ""));
    }

    @Test
    void newClientAvvisarUpptagetNamn() {
        when(repo.personExists("Anna")).thenReturn(true);
        assertThrows(BankException.class, () -> service.newClient("Anna", "", "", ""));
    }

    // ─── Nytt konto ───────────────────────────────────────────────────────────

    @Test
    void newAccountAvvisarForKortKontonummer() {
        assertThrows(BankException.class,
                () -> service.newAccount("123", "spar", "Anna", "0"));
    }

    @Test
    void newAccountAvvisarFelKontotyp() {
        when(repo.personExists("Anna")).thenReturn(true);
        assertThrows(BankException.class,
                () -> service.newAccount("12345", "check", "Anna", "0"));
    }

    @Test
    void newAccountAvvisarNegativtSaldo() {
        when(repo.personExists("Anna")).thenReturn(true);
        assertThrows(BankException.class,
                () -> service.newAccount("12345", "spar", "Anna", "-100"));
    }

    @Test
    void newAccountTillaterTomtSaldo() {
        when(repo.personExists("Anna")).thenReturn(true);
        service.newAccount("12345", "spar", "Anna", "");
        verify(repo).insertAccount("12345", "spar", "Anna", 0.0);
    }

    // ─── Ta bort ──────────────────────────────────────────────────────────────

    @Test
    void deletePersonAvvisarNarKontonFinns() {
        when(repo.personHasAccounts("Anna")).thenReturn(true);
        assertThrows(BankException.class, () -> service.deletePerson("Anna"));
        verify(repo, never()).deletePerson(anyString());
    }

    @Test
    void deleteAccountAvvisarOkantKonto() {
        when(repo.accountExists("12345")).thenReturn(false);
        assertThrows(BankException.class, () -> service.deleteAccount("12345"));
        verify(repo, never()).deleteAccount(anyString());
    }
}
