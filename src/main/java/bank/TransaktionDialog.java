package bank;

import javax.swing.*;
import java.awt.*;

public class TransaktionDialog extends JDialog {

    public enum Typ { INSATTNING, UTTAG, OVERFORING }

    private final BankService bankService;
    private final String kontonr;
    private final Typ typ;

    private JTextField txtBelopp, txtOcr, txtTillKonto;

    public TransaktionDialog(JFrame parent, BankService bankService, String kontonr, Typ typ) {
        super(parent, dialogTitle(typ), true);
        this.bankService = bankService;
        this.kontonr     = kontonr;
        this.typ         = typ;
        setSize(400, typ == Typ.OVERFORING ? 380 : 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents();
    }

    private static String dialogTitle(Typ typ) {
        if (typ == Typ.INSATTNING)  return "Insättning";
        if (typ == Typ.UTTAG)       return "Uttag";
        return "Överföring";
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header(dialogTitle(typ)), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel card = UITheme.cardPanel(new GridBagLayout());

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(0, 0, 14, 14);
        lc.gridx  = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(0, 0, 14, 0);
        fc.gridx   = 1;

        int row = 0;

        lc.gridy = fc.gridy = row++;
        card.add(UITheme.label("Kontonummer:"), lc);
        JTextField txtKonto = UITheme.textField();
        txtKonto.setText(kontonr);
        txtKonto.setEditable(false);
        txtKonto.setBackground(new Color(245, 247, 250));
        card.add(txtKonto, fc);

        if (typ == Typ.OVERFORING) {
            lc.gridy = fc.gridy = row++;
            card.add(UITheme.label("Till konto:"), lc);
            txtTillKonto = UITheme.textField();
            card.add(txtTillKonto, fc);
        }

        lc.gridy = fc.gridy = row++;
        card.add(UITheme.label("Belopp (kr):"), lc);
        txtBelopp = UITheme.textField();
        card.add(txtBelopp, fc);

        lc.gridy = fc.gridy = row++;
        card.add(UITheme.label("OCR-meddelande:"), lc);
        txtOcr = UITheme.textField();
        card.add(txtOcr, fc);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = row;
        bc.gridx = 0; bc.gridwidth = 2;
        bc.fill  = GridBagConstraints.HORIZONTAL;
        bc.insets = new Insets(8, 0, 0, 0);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(UITheme.CARD);
        JButton btnUtfor = UITheme.primaryButton("Utför");
        JButton btnAvbryt = UITheme.secondaryButton("Avbryt");
        btnUtfor.addActionListener(e -> utfor());
        btnAvbryt.addActionListener(e -> dispose());
        btnRow.add(btnUtfor);
        btnRow.add(btnAvbryt);
        card.add(btnRow, bc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        center.add(card, gbc);

        root.add(center, BorderLayout.CENTER);
        add(root);
    }

    private void utfor() {
        try {
            String belopp = txtBelopp.getText();
            String ocr    = txtOcr.getText();
            if (typ == Typ.INSATTNING) {
                bankService.deposit(kontonr, belopp, ocr);
                JOptionPane.showMessageDialog(this, "Insättning genomförd!");
            } else if (typ == Typ.UTTAG) {
                bankService.withdraw(kontonr, belopp, ocr);
                JOptionPane.showMessageDialog(this, "Uttag genomfört!");
            } else {
                bankService.transfer(kontonr, txtTillKonto.getText(), belopp, ocr);
                JOptionPane.showMessageDialog(this, "Överföring genomförd!");
            }
            dispose();
        } catch (BankException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
