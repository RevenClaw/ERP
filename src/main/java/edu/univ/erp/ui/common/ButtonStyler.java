package edu.univ.erp.ui.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * Utility for applying a consistent style to action buttons.
 */
public final class ButtonStyler {

    private static final Color PRIMARY_BG = new Color(208, 214, 230);
    private static final Color PRIMARY_FG = new Color(40, 53, 82);
    private static final Color PRIMARY_BG_HOVER = new Color(191, 199, 219);

    private ButtonStyler() {
    }

    public static void stylePrimary(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_BG);
        button.setForeground(PRIMARY_FG);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(6, 16, 6, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Font font = button.getFont();
        button.setFont(font.deriveFont(Font.BOLD, 13f));
        Dimension size = button.getPreferredSize();
        button.setPreferredSize(new Dimension(size.width + 10, size.height + 4));
        button.addChangeListener(e -> {
            if (button.getModel().isRollover()) {
                button.setBackground(PRIMARY_BG_HOVER);
            } else {
                button.setBackground(PRIMARY_BG);
            }
        });
    }
}

