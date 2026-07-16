package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Kontohantering extends JFrame {

    private final BankService bankService;
    private final Meny meny;
    private JTextField txtKonto;
    private JTextArea infoArea;
    private JButton btnInsattning, btnUttag, btnOverforing, btnTransaktioner, btnTaBort;
    private String aktivtKonto;

    public Kontohantering(BankService bankService, Meny meny) {
        this.bankService = bankService;
        this.meny        = meny;
        setTitle("Kontohantering");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { meny.setVisible(true); }
        });
        setupGUI();
    }

    private void setupGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Kontohantering"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridBagLayout());

        // --- Sökrad ---
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(0, 0, 14, 14);
        lc.gridx = 0; lc.gridy = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(0, 0, 14, 0);
        fc.gridx = 1; fc.gridy = 0;

        card.add(UITheme.label("Kontonummer:"), lc);
        txtKonto = UITheme.textField();
        card.add(txtKonto, fc);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 1; bc.gridwidth = 2;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(0, 0, 14, 0);
        JButton btnSök = UITheme.primaryButton("Sök");
        btnSök.addActionListener(this::sökKonto);
        card.add(btnSök, bc);

        // --- Infoarea ---
        infoArea = new JTextArea(5, 20);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 13));
        infoArea.setBackground(new Color(245, 247, 250));
        infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        infoArea.setLineWrap(true);

        GridBagConstraints ac = new GridBagConstraints();
        ac.gridx = 0; ac.gridy = 2; ac.gridwidth = 2;
        ac.fill = GridBagConstraints.BOTH;
        ac.weightx = 1.0; ac.weighty = 1.0;
        ac.insets = new Insets(0, 0, 14, 0);
        card.add(infoArea, ac);

        // --- Transaktionsknappar ---
        GridBagConstraints r3 = new GridBagConstraints();
        r3.gridx = 0; r3.gridy = 3; r3.gridwidth = 2;
        r3.fill = GridBagConstraints.HORIZONTAL;
        r3.insets = new Insets(0, 0, 8, 0);

        JPanel transRow = new JPanel(new GridLayout(1, 3, 8, 0));
        transRow.setBackground(UITheme.CARD);
        btnInsattning = UITheme.primaryButton("Insättning");
        btnUttag      = UITheme.primaryButton("Uttag");
        btnOverforing = UITheme.primaryButton("Överföring");
        transRow.add(btnInsattning);
        transRow.add(btnUttag);
        transRow.add(btnOverforing);
        card.add(transRow, r3);

        btnInsattning.addActionListener(e -> {
            new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.INSATTNING).setVisible(true);
            refreshKonto();
        });
        btnUttag.addActionListener(e -> {
            new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.UTTAG).setVisible(true);
            refreshKonto();
        });
        btnOverforing.addActionListener(e -> {
            new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.OVERFORING).setVisible(true);
            refreshKonto();
        });

        // --- Sekundärknappar ---
        GridBagConstraints r4 = new GridBagConstraints();
        r4.gridx = 0; r4.gridy = 4; r4.gridwidth = 2;
        r4.fill = GridBagConstraints.HORIZONTAL;
        r4.insets = new Insets(0, 0, 8, 0);

        JPanel sekundärRow = new JPanel(new GridLayout(1, 2, 8, 0));
        sekundärRow.setBackground(UITheme.CARD);
        btnTransaktioner = UITheme.secondaryButton("Visa transaktioner");
        btnTaBort        = UITheme.secondaryButton("Ta bort konto");
        sekundärRow.add(btnTransaktioner);
        sekundärRow.add(btnTaBort);
        card.add(sekundärRow, r4);

        btnTransaktioner.addActionListener(e -> visaTransaktioner());
        btnTaBort.addActionListener(e -> taBortKonto());

        // --- Tillbaka ---
        GridBagConstraints r5 = new GridBagConstraints();
        r5.gridx = 0; r5.gridy = 5; r5.gridwidth = 2;
        r5.fill = GridBagConstraints.HORIZONTAL;

        JButton btnTillbaka = UITheme.secondaryButton("Tillbaka");
        btnTillbaka.addActionListener(e -> { meny.setVisible(true); dispose(); });
        card.add(btnTillbaka, r5);

        setTransactionButtonsEnabled(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        center.add(card, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    private void sökKonto(ActionEvent evt) {
        String kontonr = txtKonto.getText();
        try {
            infoArea.setText(formatKonto(bankService.getKontoDetails(kontonr)));
            aktivtKonto = kontonr;
            setTransactionButtonsEnabled(true);
        } catch (BankException e) {
            infoArea.setText("");
            aktivtKonto = null;
            setTransactionButtonsEnabled(false);
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void refreshKonto() {
        if (aktivtKonto == null) return;
        try {
            infoArea.setText(formatKonto(bankService.getKontoDetails(aktivtKonto)));
        } catch (BankException ignored) {}
    }

    private static String formatKonto(KontoInfo konto) {
        return String.format(
            "Kontonummer:  %s%nTyp:          %s%nÄgare:        %s%nSaldo:        %.2f kr",
            konto.kontonr(), konto.kontotypDisplay(), konto.namn(), konto.saldo());
    }

    private void visaTransaktioner() {
        List<TransactionInfo> trans = bankService.getTransactions(aktivtKonto);

        JDialog dialog = new JDialog(this, "Transaktioner – konto " + aktivtKonto, true);
        dialog.setSize(560, 380);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        if (trans.isEmpty()) {
            area.setText("Inga transaktioner hittades.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (TransactionInfo t : trans) {
                sb.append(String.format("%-16s  %-10s  %9.2f kr  –  %s%n",
                    t.datumDisplay(), t.typDisplay(), t.belopp(), t.ocr()));
            }
            area.setText(sb.toString());
        }

        JButton btnStäng = UITheme.secondaryButton("Stäng");
        btnStäng.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));
        btnPanel.add(btnStäng);

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void taBortKonto() {
        int svar = JOptionPane.showConfirmDialog(this,
            "Vill du ta bort konto " + aktivtKonto + "?\nAlla transaktioner för kontot tas också bort.",
            "Bekräfta borttagning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (svar == JOptionPane.YES_OPTION) {
            try {
                bankService.deleteAccount(aktivtKonto);
                infoArea.setText("");
                aktivtKonto = null;
                setTransactionButtonsEnabled(false);
                JOptionPane.showMessageDialog(this, "Kontot har tagits bort.");
            } catch (BankException e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void setTransactionButtonsEnabled(boolean enabled) {
        btnInsattning.setEnabled(enabled);
        btnUttag.setEnabled(enabled);
        btnOverforing.setEnabled(enabled);
        btnTransaktioner.setEnabled(enabled);
        btnTaBort.setEnabled(enabled);
    }
}
