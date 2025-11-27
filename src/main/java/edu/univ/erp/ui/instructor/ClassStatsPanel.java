package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.service.InstructorService;

/**
 * Panel for displaying class statistics.
 */
public class ClassStatsPanel extends JPanel {

    private final long userId;
    private final InstructorApi instructorApi;
    private final JLabel totalStudentsLabel;
    private final JLabel classAverageLabel;
    private final JLabel medianLabel;
    private final JLabel highestLabel;
    private final JLabel lowestLabel;
    private Long currentSectionId;

    public ClassStatsPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.instructorApi = context.instructorApi();
        this.totalStudentsLabel = new JLabel("N/A");
        this.classAverageLabel = new JLabel("N/A");
        this.medianLabel = new JLabel("N/A");
        this.highestLabel = new JLabel("N/A");
        this.lowestLabel = new JLabel("N/A");

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top: instructions
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select a section from 'My Sections' tab to view statistics."));
        add(topPanel, BorderLayout.NORTH);

        // Center: statistics display
        JPanel statsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        statsPanel.add(new JLabel("Total Students:"));
        statsPanel.add(totalStudentsLabel);
        statsPanel.add(new JLabel("Class Average:"));
        statsPanel.add(classAverageLabel);
        statsPanel.add(new JLabel("Median Score:"));
        statsPanel.add(medianLabel);
        statsPanel.add(new JLabel("Highest Score:"));
        statsPanel.add(highestLabel);
        statsPanel.add(new JLabel("Lowest Score:"));
        statsPanel.add(lowestLabel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statsPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setSection(long sectionId) {
        this.currentSectionId = sectionId;
        loadStatistics();
    }

    private void loadStatistics() {
        if (currentSectionId == null) {
            totalStudentsLabel.setText("N/A");
            classAverageLabel.setText("N/A");
            medianLabel.setText("N/A");
            highestLabel.setText("N/A");
            lowestLabel.setText("N/A");
            return;
        }

        try {
            InstructorService.ClassStatistics stats = instructorApi.getClassStatistics(userId, currentSectionId);
            totalStudentsLabel.setText(String.valueOf(stats.totalStudents()));
            classAverageLabel.setText(String.format("%.2f%%", stats.average()));
            medianLabel.setText(String.format("%.2f%%", stats.median()));
            highestLabel.setText(String.format("%.2f%%", stats.highest()));
            lowestLabel.setText(String.format("%.2f%%", stats.lowest()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

