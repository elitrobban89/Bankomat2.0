package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Meny extends JFrame implements ActionListener {

    private final BankService bankService = new BankService();
    private JButton btnRegister;
    private JButton btnAccount;

    public Meny() {
        setTitle("Bankomat 2.0");
        setSize(400, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setupGUI();
        setVisible(true);
    }

    private void setupGUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Bankomat 2.0"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridLayout(2, 1, 0, 12));
        btnRegister = UITheme.primaryButton("Registervård");
        btnAccount  = UITheme.primaryButton("Kontohantering");
        btnRegister.addActionListener(this);
        btnAccount.addActionListener(this);
        card.add(btnRegister);
        card.add(btnAccount);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        center.add(card, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) {
            this.setVisible(false);
            new Val(bankService, this).setVisible(true);
        } else if (e.getSource() == btnAccount) {
            this.setVisible(false);
            new Kontohantering(bankService, this).setVisible(true);
        }
    }

    public static void main(String[] args) {
        UITheme.setup();
        SwingUtilities.invokeLater(Meny::new);
    }
}
