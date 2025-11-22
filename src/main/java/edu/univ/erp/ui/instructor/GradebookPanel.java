package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.domain.Assessment;

/**
 * Panel for viewing and editing gradebook.
 */
public class GradebookPanel extends JPanel {

    private final long userId;
    private final InstructorApi instructorApi;
    private final AssessmentRepository assessmentRepository;
    private final JTable gradebookTable;
    private final DefaultTableModel tableModel;
    private Long currentSectionId;
    private List<GradebookRow> currentRows;
    private List<Assessment> currentAssessments;

    public GradebookPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.instructorApi = context.instructorApi();
        this.assessmentRepository = context.repositoryFactory().assessmentRepository();
        this.tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing of grade columns (skip student name, roll no, final grade)
                return column > 1 && column < getColumnCount() - 1;
            }
        };
        this.gradebookTable = new JTable(tableModel);
        this.currentRows = List.of();
        this.currentAssessments = List.of();

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top: instructions
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select a section from 'My Sections' tab to view gradebook."));
        add(topPanel, BorderLayout.NORTH);

        // Center: gradebook table
        gradebookTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        gradebookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = gradebookTable.rowAtPoint(e.getPoint());
                    int col = gradebookTable.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        editGrade(row, col);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(gradebookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton computeFinalButton = new JButton("Compute Final Grades for All");
        computeFinalButton.addActionListener(e -> computeAllFinalGrades());
        bottomPanel.add(computeFinalButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadGradebook());
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setSection(long sectionId) {
        this.currentSectionId = sectionId;
        loadGradebook();
    }

    private void loadGradebook() {
        if (currentSectionId == null) {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            return;
        }

        try {
            currentRows = instructorApi.getGradebook(userId, currentSectionId);
            currentAssessments = assessmentRepository.findBySection(currentSectionId);
            updateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading gradebook: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        // Build column headers
        java.util.Vector<String> columns = new java.util.Vector<>();
        columns.add("Student Name");
        columns.add("Roll No");
        for (Assessment assessment : currentAssessments) {
            columns.add(assessment.name() + " (" + assessment.weightPercent() + "%)");
        }
        columns.add("Final Score");
        columns.add("Letter Grade");

        tableModel.setColumnIdentifiers(columns);

        // Build rows
        tableModel.setRowCount(0);
        for (GradebookRow row : currentRows) {
            java.util.Vector<Object> data = new java.util.Vector<>();
            data.add(row.studentName());
            data.add(row.rollNo());

            for (Assessment assessment : currentAssessments) {
                Double score = row.assessmentScores().get(assessment.id());
                data.add(score != null ? String.format("%.2f", score) : "");
            }

            data.add(row.finalScore() != null ? String.format("%.2f", row.finalScore()) : "");
            data.add(row.letterGrade() != null ? row.letterGrade() : "");

            tableModel.addRow(data);
        }
    }

    private void computeAllFinalGrades() {
        if (currentSectionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.",
                    "No Section Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Compute final grades for all students in this section?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (GradebookRow row : currentRows) {
            try {
                var result = instructorApi.computeFinalGrade(userId, currentSectionId, row.enrollmentId());
                if (result.success()) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                failCount++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Computed final grades: " + successCount + " successful, " + failCount + " failed.",
                "Result", JOptionPane.INFORMATION_MESSAGE);
        loadGradebook(); // Refresh
    }

    // This would be called when user edits a cell - for now, we'll use a dialog
    public void editGrade(int rowIndex, int columnIndex) {
        if (currentSectionId == null || rowIndex < 0 || rowIndex >= currentRows.size()) {
            return;
        }

        if (columnIndex < 2 || columnIndex >= 2 + currentAssessments.size()) {
            return; // Not a grade column
        }

        GradebookRow gradebookRow = currentRows.get(rowIndex);
        Assessment assessment = currentAssessments.get(columnIndex - 2);

        // Show dialog to enter/edit grade
        JTextField scoreField = new JTextField(10);
        Double currentScore = gradebookRow.assessmentScores().get(assessment.id());
        if (currentScore != null) {
            scoreField.setText(String.valueOf(currentScore));
        }

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter score for " + assessment.name() + " (max: " + assessment.maxScore() + "):"));
        panel.add(scoreField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Enter Grade", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double score = Double.parseDouble(scoreField.getText().trim());
                var enterResult = instructorApi.enterGrade(userId, currentSectionId,
                        gradebookRow.enrollmentId(), assessment.id(), score);
                if (enterResult.success()) {
                    JOptionPane.showMessageDialog(this, enterResult.message(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadGradebook(); // Refresh
                } else {
                    JOptionPane.showMessageDialog(this, enterResult.message(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid score format.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

