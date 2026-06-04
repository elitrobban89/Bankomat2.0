package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class UITheme {

    static final Color PRIMARY      = new Color(25, 118, 210);
    static final Color PRIMARY_DARK = new Color(13, 71, 161);
    static final Color BG           = new Color(235, 240, 248);
    static final Color CARD         = Color.WHITE;
    static final Color MUTED        = new Color(200, 205, 212);
    static final Color MUTED_DARK   = new Color(160, 166, 175);
    static final Color TEXT         = new Color(28, 30, 33);
    static final Color HINT         = new Color(148, 152, 160);
    static final Color BORDER_CLR   = new Color(200, 208, 220);
    static final Color FOCUS_CLR    = new Color(66, 153, 225);

    static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    /** Gradient blue header bar. */
    static JPanel header(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl);
        return panel;
    }

    /** White card panel with subtle rounded border and shadow. */
    static JPanel cardPanel(LayoutManager layout) {
        JPanel card = new JPanel(layout) {
            private static final int R = 14;
            private static final int S = 4;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth() - S, h = getHeight() - S;
                // layered shadow
                for (int i = S; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 12 * (S - i + 1)));
                    g2.fillRoundRect(i, i, w, h, R, R);
                }
                // card fill
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, w, h, R, R);
                // subtle border
                g2.setColor(new Color(210, 218, 230));
                g2.drawRoundRect(0, 0, w - 1, h - 1, R, R);
                g2.dispose();
            }

            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24 + 4, 28 + 4));
        return card;
    }

    static JButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE);
    }

    static JButton secondaryButton(String text) {
        return new RoundedButton(text, MUTED, MUTED_DARK, TEXT);
    }

    static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(TEXT);
        return lbl;
    }

    static JTextField textField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 14));
        tf.setBackground(CARD);
        applyNormalBorder(tf);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { applyFocusBorder(tf); }
            @Override public void focusLost(FocusEvent e)   { applyNormalBorder(tf); }
        });
        return tf;
    }

    private static void applyNormalBorder(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    private static void applyFocusBorder(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FOCUS_CLR, 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    // -------------------------------------------------------------------------

    static class RoundedButton extends JButton {
        private final Color bgNormal, bgHover;

        RoundedButton(String text, Color normal, Color hover, Color fg) {
            super(text);
            this.bgNormal = normal;
            this.bgHover  = hover;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(fg);
            setFont(new Font("Arial", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean pressed = getModel().isPressed();
            boolean hover   = getModel().isRollover();
            Color bg = pressed ? bgHover.darker() : hover ? bgHover : bgNormal;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override public boolean isOpaque() { return false; }
    }
}
