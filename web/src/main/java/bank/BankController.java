package bank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // ─── Meny ────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String meny() {
        return "meny";
    }

    // ─── Registervård ─────────────────────────────────────────────────────────

    @GetMapping("/registervard")
    public String registervard() {
        return "registervard";
    }

    @GetMapping("/registervard/ny-person")
    public String nyPersonForm() {
        return "ny-person";
    }

    @PostMapping("/registervard/ny-person")
    public String nyPerson(@RequestParam String name,
                           @RequestParam(defaultValue = "") String gatuadress,
                           @RequestParam(defaultValue = "") String postnr,
                           @RequestParam(defaultValue = "") String stad,
                           Model model) {
        try {
            bankService.newClient(name, gatuadress, postnr, stad);
            model.addAttribute("success", "Kontoinnehavare '" + name + "' har registrerats.");
        } catch (BankException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formName", name);
            model.addAttribute("formGatu", gatuadress);
            model.addAttribute("formPostnr", postnr);
            model.addAttribute("formStad", stad);
        }
        return "ny-person";
    }

    @GetMapping("/registervard/nytt-konto")
    public String nyttKontoForm() {
        return "nytt-konto";
    }

    @PostMapping("/registervard/nytt-konto")
    public String nyttKonto(@RequestParam String kontonr,
                            @RequestParam String kontotyp,
                            @RequestParam String namn,
                            @RequestParam(defaultValue = "") String saldo,
                            Model model) {
        try {
            bankService.newAccount(kontonr, kontotyp, namn, saldo);
            model.addAttribute("success", "Konto " + kontonr + " skapat för " + namn + ".");
        } catch (BankException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formKontonr", kontonr);
            model.addAttribute("formKontotyp", kontotyp);
            model.addAttribute("formNamn", namn);
            model.addAttribute("formSaldo", saldo);
        }
        return "nytt-konto";
    }

    @GetMapping("/registervard/personer")
    public String personer(Model model) {
        model.addAttribute("persons", bankService.getAllPersonNames());
        return "personer";
    }

    @PostMapping("/registervard/ta-bort-person")
    public String taBortPerson(@RequestParam String name, RedirectAttributes ra) {
        try {
            bankService.deletePerson(name);
            ra.addFlashAttribute("success", "'" + name + "' har tagits bort.");
        } catch (BankException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registervard/personer";
    }

    @GetMapping("/registervard/konton")
    public String kontonPerPerson(@RequestParam String namn, Model model) {
        model.addAttribute("namn", namn);
        model.addAttribute("konton", bankService.getAccountsByPerson(namn));
        model.addAttribute("totalSaldo", bankService.getTotalSaldo(namn));
        return "konton-per-person";
    }

    // ─── Kontoöversikt ────────────────────────────────────────────────────────

    @GetMapping("/kontoversikt")
    public String kontoversikt(Model model) {
        model.addAttribute("konton", bankService.getAllAccounts());
        return "kontoversikt";
    }

    // ─── Kontohantering ───────────────────────────────────────────────────────

    @GetMapping("/kontohantering")
    public String kontohantering(@RequestParam(required = false) String kontonr, Model model) {
        if (kontonr != null && !kontonr.isBlank()) {
            try {
                KontoInfo konto = bankService.getKontoDetails(kontonr);
                model.addAttribute("konto", konto);
            } catch (BankException e) {
                model.addAttribute("error", e.getMessage());
            }
            model.addAttribute("kontonr", kontonr);
        }
        return "kontohantering";
    }

    @PostMapping("/kontohantering/insattning")
    public String insattning(@RequestParam String kontonr,
                             @RequestParam String belopp,
                             @RequestParam(defaultValue = "") String ocr,
                             RedirectAttributes ra) {
        try {
            bankService.deposit(kontonr, belopp, ocr);
            ra.addFlashAttribute("success", "Insättning på " + belopp + " kr genomförd.");
        } catch (BankException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/kontohantering?kontonr=" + kontonr;
    }

    @PostMapping("/kontohantering/uttag")
    public String uttag(@RequestParam String kontonr,
                        @RequestParam String belopp,
                        @RequestParam(defaultValue = "") String ocr,
                        RedirectAttributes ra) {
        try {
            bankService.withdraw(kontonr, belopp, ocr);
            ra.addFlashAttribute("success", "Uttag på " + belopp + " kr genomfört.");
        } catch (BankException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/kontohantering?kontonr=" + kontonr;
    }

    @PostMapping("/kontohantering/overforing")
    public String overforing(@RequestParam String kontonr,
                              @RequestParam String tillKonto,
                              @RequestParam String belopp,
                              @RequestParam(defaultValue = "") String ocr,
                              RedirectAttributes ra) {
        try {
            bankService.transfer(kontonr, tillKonto, belopp, ocr);
            ra.addFlashAttribute("success",
                    "Överföring på " + belopp + " kr till konto " + tillKonto + " genomförd.");
        } catch (BankException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/kontohantering?kontonr=" + kontonr;
    }

    @GetMapping("/kontohantering/transaktioner")
    public String transaktioner(@RequestParam String kontonr, Model model) {
        model.addAttribute("kontonr", kontonr);
        model.addAttribute("transactions", bankService.getTransactions(kontonr));
        return "transaktioner";
    }

    @PostMapping("/kontohantering/ta-bort")
    public String taBortKonto(@RequestParam String kontonr, RedirectAttributes ra) {
        try {
            bankService.deleteAccount(kontonr);
            ra.addFlashAttribute("success", "Konto " + kontonr + " har tagits bort.");
        } catch (BankException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/kontohantering";
    }
}
