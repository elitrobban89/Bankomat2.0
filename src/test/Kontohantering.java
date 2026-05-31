package test;

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
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupGUI();
        setVisible(true);
    }

    private void setupGUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel rad1 = new JPanel(new GridLayout(1, 2, 10, 10));
        JLabel lbl = new JLabel("Kontonummer:");
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        txtKonto = new JTextField();
        rad1.add(lbl);
        rad1.add(txtKonto);

        JButton btnSök = new JButton("Sök");
        btnSök.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSök.addActionListener(this::sökKonto);

        JButton btnTillbaka = new JButton("Tillbaka");
        btnTillbaka.setFont(new Font("Arial", Font.PLAIN, 16));
        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });

        panel.add(rad1);
        panel.add(btnSök);
        panel.add(btnTillbaka);

        add(panel);
    }

    private void sökKonto(ActionEvent evt) {
        String kontonr = txtKonto.getText();
        try {
            List<String> kontoInfo = bankService.getKontoInfo(kontonr);
            JOptionPane.showMessageDialog(this, "Kontoinfo: " + kontoInfo);

            List<String> transaktioner = bankService.getTransactions(kontonr);
            if (transaktioner.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inga transaktioner hittades.");
            } else {
                JOptionPane.showMessageDialog(this, "Transaktioner: " + transaktioner);
            }
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
