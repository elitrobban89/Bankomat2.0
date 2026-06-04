package bank;

import javax.swing.*;
import java.awt.*;

public class NyttKontoForm extends JFrame {

    private final BankService bankService;
    private JTextField txtKontonr, txtKontotyp, txtNamn, txtSaldo;

    public NyttKontoForm(BankService bankService) {
        this.bankService = bankService;
        setTitle("Nytt konto");
        setSize(460, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Nytt konto"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridBagLayout());

        txtKontonr  = UITheme.textField();
        txtKontotyp = UITheme.textField();
        txtNamn     = UITheme.textField();
        txtSaldo    = UITheme.textField();

        String[]     labels = {"Kontonummer", "Kontotyp", "Namn", "Saldo"};
        JTextField[] fields = {txtKontonr, txtKontotyp, txtNamn, txtSaldo};
        String[]     hints  = {null, "spar eller loen", null, null};

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(0, 0, 14, 14);
        lc.gridx  = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(0, 0, 14, 0);
        fc.gridx   = 1;

        for (int i = 0; i < labels.length; i++) {
            lc.gridy = fc.gridy = i;
            card.add(UITheme.label(labels[i]), lc);

            if (hints[i] != null) {
                JPanel wrap = new JPanel(new BorderLayout(8, 0));
                wrap.setBackground(UITheme.CARD);
                JLabel hint = new JLabel(hints[i]);
                hint.setFont(new Font("Arial", Font.ITALIC, 12));
                hint.setForeground(UITheme.HINT);
                wrap.add(fields[i], BorderLayout.CENTER);
                wrap.add(hint, BorderLayout.EAST);
                card.add(wrap, fc);
            } else {
                card.add(fields[i], fc);
            }
        }

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = labels.length;
        bc.gridx = 0; bc.gridwidth = 2;
        bc.fill  = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(8, 0, 0, 0);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(UITheme.CARD);
        JButton btnSpara = UITheme.primaryButton("Spara");
        JButton btnStäng = UITheme.secondaryButton("Stäng");
        btnSpara.addActionListener(e -> spara());
        btnStäng.addActionListener(e -> dispose());
        btnRow.add(btnSpara);
        btnRow.add(btnStäng);
        card.add(btnRow, bc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        center.add(card, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    private void spara() {
        try {
            bankService.newAccount(
                    txtKontonr.getText(),
                    txtKontotyp.getText(),
                    txtNamn.getText(),
                    txtSaldo.getText()
            );
            JOptionPane.showMessageDialog(this, "Nytt konto skapat!");
            dispose();
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
