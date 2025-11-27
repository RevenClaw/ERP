package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.instructor.InstructorApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;

/**
 * Panel for viewing instructor's assigned sections.
 */
public class MySectionsPanel extends JPanel {

    private final ApplicationContext context;
    private final long userId;
    private final InstructorApi instructorApi;
    private final CourseRepository courseRepository;
    private final JTable sectionsTable;
    private final DefaultTableModel tableModel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;
    private List<Section> currentSections;
    private Runnable onSectionSelected;

    public MySectionsPanel(ApplicationContext context, long userId, Runnable onSectionSelected) {
        this.context = context;
        this.userId = userId;
        this.instructorApi = context.instructorApi();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.onSectionSelected = onSectionSelected;
        this.tableModel = new DefaultTableModel(new String[]{
                "Course", "Section Code", "Day", "Time", "Room", "Capacity", "Enrolled"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.sectionsTable = new JTable(tableModel);
        this.currentSections = List.of();

        initializeUI();
        loadSections(Term.MONSOON, 2025);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel: filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Term:"));
        termCombo = new JComboBox<>(Term.values());
        termCombo.setSelectedItem(Term.MONSOON);
        topPanel.add(termCombo);

        topPanel.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        yearCombo.setSelectedItem(2025);
        topPanel.add(yearCombo);

        javax.swing.JButton loadButton = new javax.swing.JButton("Load My Sections");
        loadButton.addActionListener(e -> loadSections());
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: sections table
        sectionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        sectionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && onSectionSelected != null) {
                onSectionSelected.run();
            }
        });
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSections() {
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            loadSections(term, year);
        }
    }

    private void loadSections(Term term, int year) {
        try {
            currentSections = instructorApi.getMySections(userId, term, year);
            updateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Section section : currentSections) {
            int enrolledCount = context.repositoryFactory().enrollmentRepository()
                    .countForSection(section.id());
            Course course = courseRepository.findById(section.courseId()).orElse(null);
            String courseCode = (course != null) ? course.code() : "Unknown";
            
            tableModel.addRow(new Object[]{
                    courseCode,
                    section.sectionCode(),
                    section.dayOfWeek().name(),
                    section.startTime() + " - " + section.endTime(),
                    section.room(),
                    section.capacity(),
                    enrolledCount
            });
        }
    }

    public Long getSelectedSectionId() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSections.size()) {
            return currentSections.get(selectedRow).id();
        }
        return null;
    }
}
