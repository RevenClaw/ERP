package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.config.ApplicationContext;

/**
 * Panel for managing maintenance mode.
 */
public class MaintenancePanel extends JPanel {

    private final AdminApi adminApi;
    private final JLabel statusLabel;
    private final JButton toggleButton;

    public MaintenancePanel(ApplicationContext context) {
        this.adminApi = context.adminApi();
        this.statusLabel = new JLabel();
        this.toggleButton = new JButton();

        initializeUI();
        updateStatus();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Center: status and toggle
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.add(new JLabel("Maintenance Mode:"));
        statusPanel.add(statusLabel);
        centerPanel.add(statusPanel);

        toggleButton.addActionListener(e -> toggleMaintenanceMode());
        centerPanel.add(toggleButton);

        add(centerPanel, BorderLayout.CENTER);

        // Instructions
        JPanel instructionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        instructionsPanel.add(new JLabel("<html><b>Note:</b> When maintenance mode is ON, students and instructors " +
                "can view data but cannot make changes (register, drop, enter grades, etc.).</html>"));
        add(instructionsPanel, BorderLayout.SOUTH);
    }

    private void updateStatus() {
        boolean isOn = adminApi.isMaintenanceMode();
        statusLabel.setText(isOn ? "ON" : "OFF");
        statusLabel.setForeground(isOn ? java.awt.Color.RED : java.awt.Color.GREEN);
        toggleButton.setText(isOn ? "Turn OFF" : "Turn ON");
    }

    private void toggleMaintenanceMode() {
        boolean currentStatus = adminApi.isMaintenanceMode();
        String action = currentStatus ? "turn OFF" : "turn ON";
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " maintenance mode?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminApi.setMaintenanceMode(!currentStatus);
                updateStatus();
                JOptionPane.showMessageDialog(this,
                        "Maintenance mode has been " + (currentStatus ? "turned OFF" : "turned ON") + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

