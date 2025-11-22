package edu.univ.erp.ui.student;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.common.MaintenanceBanner;

/**
 * Student dashboard window with tabbed interface.
 */
public final class StudentDashboard extends JFrame {

    private final LoginResponse loginResponse;
    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final JTabbedPane tabbedPane;

    public StudentDashboard(LoginResponse loginResponse, boolean maintenanceMode, String maintenanceMessage,
                           ApplicationContext context) {
        this.loginResponse = loginResponse;
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();
        this.tabbedPane = new JTabbedPane();
        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Maintenance banner
        maintenanceBanner.setMaintenanceMode(maintenanceMode, maintenanceMessage);
        mainPanel.add(maintenanceBanner, BorderLayout.NORTH);

        // Tabbed content
        long userId = loginResponse.userId();
        tabbedPane.addTab("Course Catalog", new CourseCatalogPanel(context, userId));
        tabbedPane.addTab("My Registrations", new MyRegistrationsPanel(context, userId));
        tabbedPane.addTab("Timetable", new TimetablePanel(context, userId));
        tabbedPane.addTab("Grades", new GradesPanel(context, userId));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    public void updateMaintenanceMode(boolean enabled, String message) {
        maintenanceBanner.setMaintenanceMode(enabled, message);
    }
}

