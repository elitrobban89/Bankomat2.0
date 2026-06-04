package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Kontohantering extends JFrame {

    private final BankService bankService;
    private final Meny meny;
    private JTextField txtKonto;
    private JTextArea infoArea;
    private JButton btnInsattning, btnUttag, btnOverforing, btnTransaktioner;
    private String aktivtKonto;

    public Kontohantering(BankService bankService, Meny meny) {
        this.bankService = bankService;
        this.meny        = meny;
        setTitle("Kontohantering");
        setSize(480, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
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

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(0, 0, 14, 14);
        lc.gridx  = 0; lc.gridy = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(0, 0, 14, 0);
        fc.gridx   = 1; fc.gridy = 0;

        card.add(UITheme.label("Kontonummer:"), lc);
        txtKonto = UITheme.textField();
        card.add(txtKonto, fc);

        JButton btnSök = UITheme.primaryButton("Sök");
        btnSök.addActionListener(this::sökKonto);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 1; bc.gridwidth = 2;
        bc.fill  = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(0, 0, 16, 0);
        card.add(btnSök, bc);

        infoArea = new JTextArea(4, 20);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 13));
        infoArea.setBackground(new Color(245, 247, 250));
        infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        infoArea.setLineWrap(true);

        GridBagConstraints ac = new GridBagConstraints();
        ac.gridx = 0; ac.gridy = 2; ac.gridwidth = 2;
        ac.fill  = GridBagConstraints.BOTH;
        ac.weightx = 1.0; ac.weighty = 1.0;
        ac.insets = new Insets(0, 0, 16, 0);
        card.add(infoArea, ac);

        GridBagConstraints t1c = new GridBagConstraints();
        t1c.gridx = 0; t1c.gridy = 3; t1c.gridwidth = 2;
        t1c.fill  = GridBagConstraints.HORIZONTAL;
        t1c.insets = new Insets(0, 0, 8, 0);

        JPanel transRow = new JPanel(new GridLayout(1, 3, 8, 0));
        transRow.setBackground(UITheme.CARD);
        btnInsattning  = UITheme.primaryButton("Insättning");
        btnUttag       = UITheme.primaryButton("Uttag");
        btnOverforing  = UITheme.primaryButton("Överföring");
        btnInsattning.setEnabled(false);
        btnUttag.setEnabled(false);
        btnOverforing.setEnabled(false);
        btnInsattning.addActionListener(e -> new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.INSATTNING).setVisible(true));
        btnUttag.addActionListener(e -> new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.UTTAG).setVisible(true));
        btnOverforing.addActionListener(e -> new TransaktionDialog(this, bankService, aktivtKonto, TransaktionDialog.Typ.OVERFORING).setVisible(true));
        transRow.add(btnInsattning);
        transRow.add(btnUttag);
        transRow.add(btnOverforing);
        card.add(transRow, t1c);

        GridBagConstraints t2c = new GridBagConstraints();
        t2c.gridx = 0; t2c.gridy = 4; t2c.gridwidth = 2;
        t2c.fill  = GridBagConstraints.HORIZONTAL;
        t2c.insets = new Insets(0, 0, 8, 0);

        btnTransaktioner = UITheme.secondaryButton("Visa transaktioner");
        btnTransaktioner.setEnabled(false);
        btnTransaktioner.addActionListener(e -> visaTransaktioner());
        card.add(btnTransaktioner, t2c);

        GridBagConstraints backC = new GridBagConstraints();
        backC.gridx = 0; backC.gridy = 5; backC.gridwidth = 2;
        backC.fill  = GridBagConstraints.HORIZONTAL;

        JButton btnTillbaka = UITheme.secondaryButton("Tillbaka");
        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });
        card.add(btnTillbaka, backC);

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
            List<String> kontoInfo = bankService.getKontoInfo(kontonr);
            infoArea.setText(String.join("\n", kontoInfo));
            aktivtKonto = kontonr;
            btnInsattning.setEnabled(true);
            btnUttag.setEnabled(true);
            btnOverforing.setEnabled(true);
            btnTransaktioner.setEnabled(true);
        } catch (BankException e) {
            infoArea.setText("");
            aktivtKonto = null;
            btnInsattning.setEnabled(false);
            btnUttag.setEnabled(false);
            btnOverforing.setEnabled(false);
            btnTransaktioner.setEnabled(false);
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void visaTransaktioner() {
        List<String> trans = bankService.getTransactions(aktivtKonto);
        if (trans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inga transaktioner hittades.");
        } else {
            JOptionPane.showMessageDialog(this, "Transaktioner:\n" + String.join("\n", trans));
        }
    }
}
