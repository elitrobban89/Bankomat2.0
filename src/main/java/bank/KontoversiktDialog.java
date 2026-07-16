package bank;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class KontoversiktDialog extends JDialog {

    public KontoversiktDialog(JFrame parent, BankService bankService) {
        super(parent, "Kontoöversikt", true);
        setSize(560, 460);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents(bankService);
    }

    private void initComponents(BankService bankService) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);
        root.add(UITheme.header("Kontoöversikt"), BorderLayout.NORTH);

        List<KontoInfo> konton = bankService.getAllAccounts();

        String[] kolumner = {"Kontonummer", "Typ", "Innehavare", "Saldo (kr)"};
        DefaultTableModel model = new DefaultTableModel(kolumner, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        double totalt = 0;
        for (KontoInfo k : konton) {
            model.addRow(new Object[]{
                k.kontonr(), k.kontotypDisplay(), k.namn(), String.format("%.2f", k.saldo())});
            totalt += k.saldo();
        }

        JTable tabell = new JTable(model);
        tabell.setFont(new Font("Arial", Font.PLAIN, 13));
        tabell.setRowHeight(26);
        tabell.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabell.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabell);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_CLR));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(20, 28, 0, 28));
        if (konton.isEmpty()) {
            JLabel tom = UITheme.label("Inga konton registrerade ännu.");
            tom.setHorizontalAlignment(SwingConstants.CENTER);
            center.add(tom, BorderLayout.CENTER);
        } else {
            center.add(scroll, BorderLayout.CENTER);
            JLabel lblTotal = UITheme.label(
                String.format("%d konton  –  totalt saldo: %.2f kr", konton.size(), totalt));
            center.add(lblTotal, BorderLayout.SOUTH);
        }
        root.add(center, BorderLayout.CENTER);

        JButton btnStäng = UITheme.secondaryButton("Stäng");
        btnStäng.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(14, 28, 20, 28));
        btnPanel.add(btnStäng);
        root.add(btnPanel, BorderLayout.SOUTH);

        add(root);
    }
}
