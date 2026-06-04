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

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(14, 28, 20, 28));
        JButton btnTaBort = UITheme.primaryButton("Ta bort vald");
        JButton btnStäng  = UITheme.secondaryButton("Stäng");
        btnTaBort.addActionListener(e -> taBort());
        btnStäng.addActionListener(e -> dispose());
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
