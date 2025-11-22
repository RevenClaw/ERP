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
    private final JLabel overallAverageLabel;
    private final JLabel finalGradeAverageLabel;
    private final JLabel firstAssessmentLabel;
    private final JLabel secondAssessmentLabel;
    private Long currentSectionId;

    public ClassStatsPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.instructorApi = context.instructorApi();
        this.totalStudentsLabel = new JLabel("N/A");
        this.overallAverageLabel = new JLabel("N/A");
        this.finalGradeAverageLabel = new JLabel("N/A");
        this.firstAssessmentLabel = new JLabel("N/A");
        this.secondAssessmentLabel = new JLabel("N/A");

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
        statsPanel.add(new JLabel("Overall Average:"));
        statsPanel.add(overallAverageLabel);
        statsPanel.add(new JLabel("Final Grade Average:"));
        statsPanel.add(finalGradeAverageLabel);
        statsPanel.add(new JLabel("First Assessment Average:"));
        statsPanel.add(firstAssessmentLabel);
        statsPanel.add(new JLabel("Second Assessment Average:"));
        statsPanel.add(secondAssessmentLabel);

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
            overallAverageLabel.setText("N/A");
            finalGradeAverageLabel.setText("N/A");
            firstAssessmentLabel.setText("N/A");
            secondAssessmentLabel.setText("N/A");
            return;
        }

        try {
            InstructorService.ClassStatistics stats = instructorApi.getClassStatistics(userId, currentSectionId);
            totalStudentsLabel.setText(String.valueOf(stats.totalStudents()));
            overallAverageLabel.setText(String.format("%.2f%%", stats.overallAverage()));
            finalGradeAverageLabel.setText(String.format("%.2f%%", stats.finalGradeAverage()));
            firstAssessmentLabel.setText(String.format("%.2f", stats.firstAssessmentAverage()));
            secondAssessmentLabel.setText(String.format("%.2f", stats.secondAssessmentAverage()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

