package bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record TransactionInfo(String typ, double belopp, String ocr, LocalDateTime createdAt) {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String typDisplay() {
        return "ins".equals(typ) ? "Insättning" : "Uttag";
    }

    public String datumDisplay() {
        return createdAt != null ? createdAt.format(FMT) : "–";
    }
}
