package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Meny extends JFrame implements ActionListener {
    private JButton btnRegister;
    private JButton btnAccount;

    public Meny() {
        setTitle("Bankomat Meny");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupGUI();
        setVisible(true);
    }

    private void setupGUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        btnRegister = createButton("Registervård", this);
        btnAccount = createButton("Kontohantering", this);

        panel.add(btnRegister);
        panel.add(btnAccount);

        add(panel);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.addActionListener(listener);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) {
            new Val(this).setVisible(true);   // SKICKA MED MENY
            this.setVisible(false);
        } else if (e.getSource() == btnAccount) {
            new Kontohantering(this).setVisible(true);
            this.setVisible(false);
        }
    }

    public static void main(String[] args) {
        new Meny();
    }
}
