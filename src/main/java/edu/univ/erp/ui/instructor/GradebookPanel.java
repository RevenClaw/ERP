package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.api.types.GradebookRow;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.TableStyler;
import com.opencsv.CSVWriter;

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
    private boolean suppressTableEvents;

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
        TableStyler.apply(gradebookTable);
        this.gradebookTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        this.gradebookTable.setDefaultEditor(Object.class, new ScoreCellEditor());
        this.gradebookTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "commitEditorValue");
        this.gradebookTable.getActionMap().put("commitEditorValue", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (gradebookTable.isEditing()) {
                    gradebookTable.getCellEditor().stopCellEditing();
                }
            }
        });
        this.currentRows = List.of();
        this.currentAssessments = List.of();
        this.suppressTableEvents = false;
        this.tableModel.addTableModelListener(this::handleTableEdit);

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
        JScrollPane scrollPane = new JScrollPane(gradebookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadGradebook());
        ButtonStyler.stylePrimary(refreshButton);
        bottomPanel.add(refreshButton);

        JButton manageWeightsButton = new JButton("Manage Assessment Weights");
        manageWeightsButton.addActionListener(e -> showManageWeightsDialog());
        ButtonStyler.stylePrimary(manageWeightsButton);
        bottomPanel.add(manageWeightsButton);

        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> exportGradebook());
        ButtonStyler.stylePrimary(exportButton);
        bottomPanel.add(exportButton);

        JButton computeFinalButton = new JButton("Compute Final Grades for All");
        computeFinalButton.addActionListener(e -> computeAllFinalGrades());
        ButtonStyler.stylePrimary(computeFinalButton);
        bottomPanel.add(computeFinalButton);

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
        suppressTableEvents = true;
        try {
            Vector<String> columns = new Vector<>();
            columns.add("Student");
            columns.add("Roll No");
            for (Assessment assessment : currentAssessments) {
                columns.add(assessment.name() + " (" + String.format("%.0f%%", assessment.weightPercent()) + ")");
            }
            columns.add("Final Score");
            columns.add("Letter Grade");

            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            for (GradebookRow row : currentRows) {
                Vector<Object> data = new Vector<>();
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
        } finally {
            suppressTableEvents = false;
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

    private void showManageWeightsDialog() {
        if (currentSectionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.",
                    "No Section Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentAssessments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assessments available for this section.",
                    "No Assessments", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(currentAssessments.size(), 2, 10, 10));
        Map<Long, JTextField> fields = new LinkedHashMap<>();
        for (Assessment assessment : currentAssessments) {
            panel.add(new JLabel(assessment.name()));
            JTextField field = new JTextField(String.format("%.2f", assessment.weightPercent()));
            fields.put(assessment.id(), field);
            panel.add(field);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Manage Assessment Weights", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Map<Long, Double> weights = new HashMap<>();
            for (var entry : fields.entrySet()) {
                double weight = Double.parseDouble(entry.getValue().getText().trim());
                weights.put(entry.getKey(), weight);
            }
            var response = instructorApi.updateAssessmentWeights(userId, currentSectionId, weights);
            if (response.success()) {
                JOptionPane.showMessageDialog(this, response.message(), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadGradebook();
            } else {
                JOptionPane.showMessageDialog(this, response.message(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter numeric weights.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportGradebook() {
        if (currentSectionId == null || currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("section-" + currentSectionId + "-gradebook.csv"));
        int result = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            List<String> header = new java.util.ArrayList<>();
            header.add("Student");
            header.add("Roll No");
            for (Assessment assessment : currentAssessments) {
                header.add(assessment.name());
            }
            header.add("Final Score");
            header.add("Letter Grade");
            writer.writeNext(header.toArray(new String[0]));

            for (GradebookRow row : currentRows) {
                List<String> data = new java.util.ArrayList<>();
                data.add(row.studentName());
                data.add(row.rollNo());
                for (Assessment assessment : currentAssessments) {
                    Double score = row.assessmentScores().get(assessment.id());
                    data.add(score != null ? String.format("%.2f", score) : "");
                }
                data.add(row.finalScore() != null ? String.format("%.2f", row.finalScore()) : "");
                data.add(row.letterGrade() != null ? row.letterGrade() : "");
                writer.writeNext(data.toArray(new String[0]));
            }
            JOptionPane.showMessageDialog(this, "Gradebook exported successfully.",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export gradebook: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTableEdit(TableModelEvent event) {
        if (suppressTableEvents || event.getType() != TableModelEvent.UPDATE) {
            return;
        }
        int row = event.getFirstRow();
        int column = event.getColumn();
        if (row < 0 || column < 0) {
            return;
        }
        Object value = tableModel.getValueAt(row, column);
        String text = value != null ? value.toString() : "";
        submitScore(row, column, text);
    }

    private final class ScoreCellEditor extends DefaultCellEditor {
        private final JTextField editorField;

        ScoreCellEditor() {
            super(new JTextField());
            editorField = (JTextField) getComponent();
            editorField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 190, 210)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            editorField.setHorizontalAlignment(JTextField.LEFT);
            editorField.setFont(editorField.getFont().deriveFont(Font.PLAIN, 13f));
            editorField.setForeground(new Color(20, 30, 60));
            editorField.addActionListener(e -> stopCellEditing());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            String text = value != null ? value.toString() : "";
            editorField.setText(text);
            editorField.selectAll();
            editorField.setToolTipText("Enter score and press Enter to save");
            return editorField;
        }
    }

    private void submitScore(int rowIndex, int columnIndex, String text) {
        if (currentSectionId == null || rowIndex < 0 || rowIndex >= currentRows.size()) {
            return;
        }
        if (columnIndex < 2 || columnIndex >= 2 + currentAssessments.size()) {
            return;
        }

        GradebookRow gradebookRow = currentRows.get(rowIndex);
        Assessment assessment = currentAssessments.get(columnIndex - 2);
        String trimmed = text != null ? text.trim() : "";

        if (trimmed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Score cannot be empty.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            loadGradebook();
            return;
        }

        double score;
        try {
            score = Double.parseDouble(trimmed);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a numeric score.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            loadGradebook();
            return;
        }

        double maxAllowed = Math.min(assessment.maxScore(), 100.0);
        if (score < 0 || score > maxAllowed) {
            JOptionPane.showMessageDialog(this,
                    "Score must be between 0 and " + maxAllowed + ".",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            loadGradebook();
            return;
        }

        var result = instructorApi.enterGrade(userId, currentSectionId,
                gradebookRow.enrollmentId(), assessment.id(), score);
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.message(), "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        loadGradebook();
    }
}

