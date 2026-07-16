package bank;

public record KontoInfo(String kontonr, String kontotyp, String namn, double saldo) {
    public String kontotypDisplay() {
        return "spar".equals(kontotyp) ? "Sparkonto" : "Lönekonto";
    }
}
