package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.common.MaintenanceBanner;

/**
 * Admin dashboard window with tabbed interface.
 */
public final class AdminDashboard extends JFrame {

    private final LoginResponse loginResponse;
    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final JTabbedPane tabbedPane;

    public AdminDashboard(LoginResponse loginResponse, boolean maintenanceMode, String maintenanceMessage,
                         ApplicationContext context) {
        this.loginResponse = loginResponse;
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();
        this.tabbedPane = new JTabbedPane();
        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Maintenance banner
        maintenanceBanner.setMaintenanceMode(maintenanceMode, maintenanceMessage);
        mainPanel.add(maintenanceBanner, BorderLayout.NORTH);

        // Tabbed content
        tabbedPane.addTab("User Management", new UserManagementPanel(context));
        tabbedPane.addTab("Course Management", new CourseManagementPanel(context));
        tabbedPane.addTab("Section Management", new SectionManagementPanel(context));
        tabbedPane.addTab("Maintenance Mode", new MaintenancePanel(context));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    public void updateMaintenanceMode(boolean enabled, String message) {
        maintenanceBanner.setMaintenanceMode(enabled, message);
    }
}
