package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.common.MaintenanceBanner;

/**
 * Instructor dashboard window with tabbed interface.
 */
public final class InstructorDashboard extends JFrame {

    private final LoginResponse loginResponse;
    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final JTabbedPane tabbedPane;
    private final MySectionsPanel mySectionsPanel;
    private final GradebookPanel gradebookPanel;
    private final ClassStatsPanel classStatsPanel;

    public InstructorDashboard(LoginResponse loginResponse, boolean maintenanceMode, String maintenanceMessage,
                              ApplicationContext context) {
        this.loginResponse = loginResponse;
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();
        this.tabbedPane = new JTabbedPane();
        
        long userId = loginResponse.userId();
        this.mySectionsPanel = new MySectionsPanel(context, userId, this::onSectionSelected);
        this.gradebookPanel = new GradebookPanel(context, userId);
        this.classStatsPanel = new ClassStatsPanel(context, userId);
        
        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Maintenance banner
        maintenanceBanner.setMaintenanceMode(maintenanceMode, maintenanceMessage);
        mainPanel.add(maintenanceBanner, BorderLayout.NORTH);

        // Tabbed content
        tabbedPane.addTab("My Sections", mySectionsPanel);
        tabbedPane.addTab("Gradebook", gradebookPanel);
        tabbedPane.addTab("Class Statistics", classStatsPanel);
        
        // Update other tabs when section is selected
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSelectedSection();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void onSectionSelected() {
        updateSelectedSection();
    }

    private void updateSelectedSection() {
        Long sectionId = mySectionsPanel.getSelectedSectionId();
        if (sectionId != null) {
            gradebookPanel.setSection(sectionId);
            classStatsPanel.setSection(sectionId);
        }
    }

    public void updateMaintenanceMode(boolean enabled, String message) {
        maintenanceBanner.setMaintenanceMode(enabled, message);
    }
}

