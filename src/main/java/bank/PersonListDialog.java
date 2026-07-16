package bank;

import javax.swing.*;
import java.awt.*;

public class PersonListDialog extends JDialog {

    private final BankService bankService;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> personList = new JList<>(listModel);

    public PersonListDialog(JFrame parent, BankService bankService) {
        super(parent, "Kontoinnehavare", true);
        this.bankService = bankService;
        setSize(400, 460);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents();
        ladda();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Kontoinnehavare"), BorderLayout.NORTH);

        personList.setFont(new Font("Arial", Font.PLAIN, 14));
        personList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        personList.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane scroll = new JScrollPane(personList);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_CLR));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(20, 28, 0, 28));
        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(14, 28, 20, 28));
        JButton btnKonton = UITheme.primaryButton("Visa konton");
        JButton btnTaBort = UITheme.primaryButton("Ta bort vald");
        JButton btnStäng  = UITheme.secondaryButton("Stäng");
        btnKonton.addActionListener(e -> visaKonton());
        btnTaBort.addActionListener(e -> taBort());
        btnStäng.addActionListener(e -> dispose());
        btnPanel.add(btnKonton);
        btnPanel.add(btnTaBort);
        btnPanel.add(btnStäng);
        root.add(btnPanel, BorderLayout.SOUTH);

        add(root);
    }

    private void ladda() {
        listModel.clear();
        bankService.getAllPersonNames().forEach(listModel::addElement);
        if (listModel.isEmpty()) {
            listModel.addElement("(Inga kontoinnehavare registrerade)");
        }
    }

    private void visaKonton() {
        String vald = personList.getSelectedValue();
        if (vald == null || vald.startsWith("(")) {
            JOptionPane.showMessageDialog(this, "Välj en kontoinnehavare först.");
            return;
        }

        java.util.List<KontoInfo> konton = bankService.getAccountsByPerson(vald);

        JDialog dialog = new JDialog(this, "Konton – " + vald, true);
        dialog.setSize(480, 340);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        if (konton.isEmpty()) {
            area.setText("Inga konton registrerade för " + vald + ".");
        } else {
            StringBuilder sb = new StringBuilder();
            for (KontoInfo k : konton) {
                sb.append(String.format("%-14s  %-10s  %12.2f kr%n",
                    k.kontonr(), k.kontotypDisplay(), k.saldo()));
            }
            sb.append(String.format("%n%-26s  %12.2f kr",
                "Totalt saldo:", bankService.getTotalSaldo(vald)));
            area.setText(sb.toString());
        }

        JButton btnStängKonton = UITheme.secondaryButton("Stäng");
        btnStängKonton.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 14, 0));
        btnPanel.add(btnStängKonton);

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void taBort() {
        String vald = personList.getSelectedValue();
        if (vald == null || vald.startsWith("(")) {
            JOptionPane.showMessageDialog(this, "Välj en kontoinnehavare först.");
            return;
        }
        int svar = JOptionPane.showConfirmDialog(this,
            "Vill du ta bort " + vald + "?",
            "Bekräfta borttagning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (svar == JOptionPane.YES_OPTION) {
            try {
                bankService.deletePerson(vald);
                ladda();
            } catch (BankException e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }
}
