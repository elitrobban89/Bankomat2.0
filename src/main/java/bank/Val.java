package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Val extends JFrame implements ActionListener {

    private JButton btnNyKontoinnehavare;
    private JButton btnNyttKonto;
    private JButton btnTillbaka;
    private Meny meny;

    public Val(Meny meny) {
        this.meny = meny;
        setTitle("Registervård");
        setSize(400, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setupGUI();
        setVisible(true);
    }

    private void setupGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Registervård"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridLayout(3, 1, 0, 12));
        btnNyKontoinnehavare = UITheme.primaryButton("Ny kontoinnehavare");
        btnNyttKonto         = UITheme.primaryButton("Nytt konto");
        btnTillbaka          = UITheme.secondaryButton("Tillbaka");
        btnNyKontoinnehavare.addActionListener(this);
        btnNyttKonto.addActionListener(this);
        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });
        card.add(btnNyKontoinnehavare);
        card.add(btnNyttKonto);
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
            new NewJFrame3().setVisible(true);
            this.setVisible(false);
        }
        if (e.getSource() == btnNyttKonto) {
            JOptionPane.showMessageDialog(this, "Skapar nytt konto...");
        }
    }
}
