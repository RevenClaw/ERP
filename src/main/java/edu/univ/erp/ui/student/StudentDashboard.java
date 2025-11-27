package edu.univ.erp.ui.student;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.ApplicationLauncher;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.DashboardCard;
import edu.univ.erp.ui.common.MaintenanceBanner;

/**
 * Student dashboard window with card-based navigation.
 */
public final class StudentDashboard extends JFrame {

    private static final String SECTION_COURSE_CATALOG = "section.courseCatalog";
    private static final String SECTION_REGISTRATIONS = "section.registrations";
    private static final String SECTION_TIMETABLE = "section.timetable";
    private static final String SECTION_GRADES = "section.grades";

    private final LoginResponse loginResponse;
    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final Map<String, DashboardCard> navigationCards = new LinkedHashMap<>();
    private CardLayout contentLayout;
    private JPanel contentPanel;

    public StudentDashboard(LoginResponse loginResponse, boolean maintenanceMode, String maintenanceMessage,
                           ApplicationContext context) {
        this.loginResponse = loginResponse;
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();
        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 860);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // Maintenance banner
        maintenanceBanner.setMaintenanceMode(maintenanceMode, maintenanceMessage);
        mainPanel.add(maintenanceBanner, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 20));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));

        long userId = loginResponse.userId();
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);

        addNavigationCard(cardsPanel, SECTION_COURSE_CATALOG, "My Courses",
                "Browse offerings and register for upcoming term.",
                new CourseCatalogPanel(context, userId));
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_REGISTRATIONS, "My Registrations",
                "Track enrolled sections and enrollment status.",
                new MyRegistrationsPanel(context, userId));
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_TIMETABLE, "My Timetable",
                "Visual weekly schedule for your registered courses.",
                new TimetablePanel(context, userId));
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_GRADES, "My Grades",
                "View grades, assessments, and download transcripts.",
                new GradesPanel(context, userId));

        JButton logoutButton = new JButton("Logout");
        ButtonStyler.stylePrimary(logoutButton);
        logoutButton.addActionListener(e -> logout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(cardsPanel, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        bodyPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);
        contentWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        bodyPanel.add(contentWrapper, BorderLayout.CENTER);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        add(mainPanel);

        showSection(SECTION_COURSE_CATALOG);
    }

    private void addNavigationCard(JPanel container, String key, String title, String description, JPanel panel) {
        DashboardCard card = new DashboardCard(title, description, () -> showSection(key));
        card.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);
        navigationCards.put(key, card);
        container.add(card);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.CENTER);
        contentPanel.add(wrapper, key);
    }

    private void showSection(String key) {
        contentLayout.show(contentPanel, key);
        navigationCards.forEach((sectionKey, card) -> card.setActive(sectionKey.equals(key)));
    }

    public void updateMaintenanceMode(boolean enabled, String message) {
        maintenanceBanner.setMaintenanceMode(enabled, message);
    }

    private void logout() {
        dispose();
        new ApplicationLauncher(context).launch();
    }
}

