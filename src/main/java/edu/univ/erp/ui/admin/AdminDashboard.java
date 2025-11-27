package edu.univ.erp.ui.admin;

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

import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.ApplicationLauncher;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.DashboardCard;
import edu.univ.erp.ui.common.MaintenanceBanner;

/**
 * Admin dashboard window with card navigation.
 */
public final class AdminDashboard extends JFrame {

    private static final String SECTION_USERS = "section.users";
    private static final String SECTION_COURSES = "section.courses";
    private static final String SECTION_SECTIONS = "section.sections";
    private static final String SECTION_MAINTENANCE = "section.maintenance";

    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final Map<String, DashboardCard> navigationCards = new LinkedHashMap<>();
    private final UserManagementPanel userManagementPanel;
    private final CourseManagementPanel courseManagementPanel;
    private final SectionManagementPanel sectionManagementPanel;
    private final MaintenancePanel maintenancePanel;
    private CardLayout contentLayout;
    private JPanel contentPanel;

    public AdminDashboard(boolean maintenanceMode, String maintenanceMessage,
                         ApplicationContext context) {
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();
        this.userManagementPanel = new UserManagementPanel(context);
        this.courseManagementPanel = new CourseManagementPanel(context);
        this.sectionManagementPanel = new SectionManagementPanel(context);
        this.maintenancePanel = new MaintenancePanel(context);
        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        maintenanceBanner.setMaintenanceMode(maintenanceMode, maintenanceMessage);
        mainPanel.add(maintenanceBanner, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 20));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);

        addNavigationCard(cardsPanel, SECTION_USERS, "Manage Users",
                "Create, edit, or deactivate student and instructor accounts.",
                userManagementPanel);
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_COURSES, "Manage Courses",
                "Maintain course catalog entries and credits.",
                courseManagementPanel);
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_SECTIONS, "Manage Sections",
                "Assign instructors, set schedules, adjust capacities.",
                sectionManagementPanel);
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_MAINTENANCE, "Maintenance Mode",
                "Toggle maintenance and communicate downtime.",
                maintenancePanel);

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

        showSection(SECTION_USERS);
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
        if (enabled) {
            showSection(SECTION_MAINTENANCE);
        }
    }

    private void logout() {
        dispose();
        new ApplicationLauncher(context).launch();
    }
}
