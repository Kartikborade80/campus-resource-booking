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
}
