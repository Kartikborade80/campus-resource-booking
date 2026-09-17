package ui;

import javax.swing.*;
import java.awt.*;

public class UIStyle {
    public static final Color BUTTON_BG = Color.WHITE;
    public static final Color BUTTON_FG = new Color(30, 58, 138);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public static JButton createButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn);
        return btn;
    }

    public static JButton styleButton(JButton btn) {
        btn.setBackground(BUTTON_BG);
        btn.setForeground(BUTTON_FG);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton styleSuccessButton(JButton btn) {
        return styleButton(btn);
    }

    public static JButton styleDangerButton(JButton btn) {
        return styleButton(btn);
    }

    public static JButton stylePrimaryButton(JButton btn) {
        return styleButton(btn);
    }

    public static javax.swing.table.DefaultTableCellRenderer createStatusBadgeRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setOpaque(true);

                if (value == null) {
                    lbl.setText("-");
                    return lbl;
                }

                String status = value.toString().trim().toUpperCase();
                if (status.contains("PENDING")) {
                    lbl.setText("⏳ PENDING");
                    lbl.setBackground(isSelected ? new Color(253, 230, 138) : new Color(254, 243, 199));
                    lbl.setForeground(new Color(180, 83, 9));
                } else if (status.contains("APPROVED")) {
                    lbl.setText("✔ APPROVED");
                    lbl.setBackground(isSelected ? new Color(187, 247, 208) : new Color(220, 252, 231));
                    lbl.setForeground(new Color(21, 128, 61));
                } else if (status.contains("REJECTED")) {
                    lbl.setText("✖ REJECTED");
                    lbl.setBackground(isSelected ? new Color(254, 202, 202) : new Color(254, 226, 226));
                    lbl.setForeground(new Color(185, 28, 28));
                } else if (status.contains("COMPLETED")) {
                    lbl.setText("★ COMPLETED");
                    lbl.setBackground(isSelected ? new Color(186, 230, 253) : new Color(224, 242, 254));
                    lbl.setForeground(new Color(3, 105, 161));
                } else if (status.contains("CANCELLED")) {
                    lbl.setText("⊘ CANCELLED");
                    lbl.setBackground(isSelected ? new Color(226, 232, 240) : new Color(241, 245, 249));
                    lbl.setForeground(new Color(100, 116, 139));
                } else {
                    lbl.setText(status);
                    lbl.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    lbl.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return lbl;
            }
        };
    }
}
