package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Val extends JFrame implements ActionListener {

    private final BankService bankService;
    private final Meny meny;
    private JButton btnNyKontoinnehavare;
    private JButton btnNyttKonto;
    private JButton btnVisaPersoner;
    private JButton btnTillbaka;

    public Val(BankService bankService, Meny meny) {
        this.bankService = bankService;
        this.meny        = meny;
        setTitle("Registervård");
        setSize(400, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setupGUI();
    }

    private void setupGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Registervård"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridLayout(4, 1, 0, 12));
        btnNyKontoinnehavare = UITheme.primaryButton("Ny kontoinnehavare");
        btnNyttKonto         = UITheme.primaryButton("Nytt konto");
        btnVisaPersoner      = UITheme.secondaryButton("Visa kontoinnehavare");
        btnTillbaka          = UITheme.secondaryButton("Tillbaka");

        btnNyKontoinnehavare.addActionListener(this);
        btnNyttKonto.addActionListener(this);
        btnVisaPersoner.addActionListener(this);
        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });

        card.add(btnNyKontoinnehavare);
        card.add(btnNyttKonto);
        card.add(btnVisaPersoner);
        card.add(btnTillbaka);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        center.add(card, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnNyKontoinnehavare) {
            new NyPersonForm(bankService).setVisible(true);
        } else if (e.getSource() == btnNyttKonto) {
            new NyttKontoForm(bankService).setVisible(true);
        } else if (e.getSource() == btnVisaPersoner) {
            new PersonListDialog(this, bankService).setVisible(true);
        }
    }
}
