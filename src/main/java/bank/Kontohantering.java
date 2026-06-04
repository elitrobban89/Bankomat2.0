package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Kontohantering extends JFrame {

    private final BankService bankService = new BankService();
    private Meny meny;
    private JTextField txtKonto;

    public Kontohantering(Meny meny) {
        this.meny = meny;
        setTitle("Kontohantering");
        setSize(440, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setupGUI();
        setVisible(true);
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
        lc.insets = new Insets(0, 0, 16, 14);
        lc.gridx = 0; lc.gridy = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(0, 0, 16, 0);
        fc.gridx = 1; fc.gridy = 0;

        card.add(UITheme.label("Kontonummer:"), lc);
        txtKonto = UITheme.textField();
        card.add(txtKonto, fc);

        lc.gridy = 1; lc.gridwidth = 1;
        fc.gridy = 1;

        JButton btnSök      = UITheme.primaryButton("Sök");
        JButton btnTillbaka = UITheme.secondaryButton("Tillbaka");

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 1;
        bc.gridwidth = 2;
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(4, 0, 0, 0);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(UITheme.CARD);
        btnRow.add(btnSök);
        btnRow.add(btnTillbaka);
        card.add(btnRow, bc);

        btnSök.addActionListener(this::sökKonto);
        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });

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
            JOptionPane.showMessageDialog(this, "Kontoinfo:\n" + String.join("\n", kontoInfo));

            List<String> transaktioner = bankService.getTransactions(kontonr);
            if (transaktioner.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inga transaktioner hittades.");
            } else {
                JOptionPane.showMessageDialog(this, "Transaktioner:\n" + String.join("\n", transaktioner));
            }
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
