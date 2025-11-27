package edu.univ.erp.ui.instructor;

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
 * Instructor dashboard window with card-based navigation.
 */
public final class InstructorDashboard extends JFrame {

    private static final String SECTION_MY_SECTIONS = "section.mySections";
    private static final String SECTION_GRADEBOOK = "section.gradebook";
    private static final String SECTION_STATS = "section.stats";

    private final LoginResponse loginResponse;
    private final MaintenanceBanner maintenanceBanner;
    private final ApplicationContext context;
    private final MySectionsPanel mySectionsPanel;
    private final GradebookPanel gradebookPanel;
    private final ClassStatsPanel classStatsPanel;
    private final Map<String, DashboardCard> navigationCards = new LinkedHashMap<>();
    private CardLayout contentLayout;
    private JPanel contentPanel;

    public InstructorDashboard(LoginResponse loginResponse, boolean maintenanceMode, String maintenanceMessage,
                              ApplicationContext context) {
        this.loginResponse = loginResponse;
        this.context = context;
        this.maintenanceBanner = new MaintenanceBanner();

        long userId = loginResponse.userId();
        this.mySectionsPanel = new MySectionsPanel(context, userId, this::onSectionSelected);
        this.gradebookPanel = new GradebookPanel(context, userId);
        this.classStatsPanel = new ClassStatsPanel(context, userId);

        initializeUI(maintenanceMode, maintenanceMessage);
    }

    private void initializeUI(boolean maintenanceMode, String maintenanceMessage) {
        setTitle("University ERP - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 860);
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

        addNavigationCard(cardsPanel, SECTION_MY_SECTIONS, "My Sections",
                "View assigned sections filtered by term and year.",
                mySectionsPanel);
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_GRADEBOOK, "Gradebook",
                "Enter component scores, adjust weights, export CSV.",
                gradebookPanel);
        cardsPanel.add(Box.createHorizontalStrut(16));
        addNavigationCard(cardsPanel, SECTION_STATS, "Class Statistics",
                "Track class averages, medians, highs and lows.",
                classStatsPanel);

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

        showSection(SECTION_MY_SECTIONS);
        updateSelectedSection();
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
        updateSelectedSection();
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

    private void logout() {
        dispose();
        new ApplicationLauncher(context).launch();
    }
}

