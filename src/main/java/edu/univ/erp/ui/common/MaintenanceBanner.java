package edu.univ.erp.ui.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Banner component that displays maintenance mode status.
 */
public final class MaintenanceBanner extends JPanel {

    private static final Color MAINTENANCE_COLOR = new Color(255, 200, 0); // Amber/yellow
    private final JLabel messageLabel;

    public MaintenanceBanner() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setBackground(MAINTENANCE_COLOR);
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));

        messageLabel = new JLabel();
        messageLabel.setFont(messageLabel.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        add(messageLabel);

        setVisible(false);
    }

    public void setMaintenanceMode(boolean enabled, String message) {
        if (enabled) {
            messageLabel.setText("⚠ " + message);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}

