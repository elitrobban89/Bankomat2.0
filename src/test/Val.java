package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Val extends JFrame implements ActionListener {

    private JButton btnNyKontoinnehavare;
    private JButton btnNyttKonto;
    private JButton btnTillbaka;

    private Meny meny;

    public Val(Meny meny) {
        this.meny = meny;

        setTitle("Registervård: Välj alternativ");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // centrera fönstret

        setupGUI();
        setVisible(true);
    }

    private void setupGUI() {

        // Huvudpanel med 3 rader
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        btnNyKontoinnehavare = createButton("Ny Kontoinnehavare");
        btnNyttKonto = createButton("Nytt Konto");
        btnTillbaka = createButton("Tillbaka");

        // ActionListeners
        btnNyKontoinnehavare.addActionListener(this);
        btnNyttKonto.addActionListener(this);

        btnTillbaka.addActionListener(e -> {
            this.setVisible(false);
            meny.setVisible(true);
        });

        panel.add(btnNyKontoinnehavare);
        panel.add(btnNyttKonto);
        panel.add(btnTillbaka);

        add(panel);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        return btn;
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
