package edu.univ.erp.ui.student;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.TableStyler;

/**
 * Panel for viewing and managing student enrollments.
 */
public class MyRegistrationsPanel extends JPanel {

    private final long userId;
    private final StudentApi studentApi;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final JTable enrollmentsTable;
    private final DefaultTableModel tableModel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;
    private List<Enrollment> currentEnrollments;

    public MyRegistrationsPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.studentApi = context.studentApi();
        this.enrollmentRepository = context.repositoryFactory().enrollmentRepository();
        this.studentRepository = context.repositoryFactory().studentRepository();
        this.sectionRepository = context.repositoryFactory().sectionRepository();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.tableModel = new DefaultTableModel(new String[]{
                "Course", "Section", "Status", "Enrolled Date", "Actions"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.enrollmentsTable = new JTable(tableModel);
        TableStyler.apply(enrollmentsTable);
        this.currentEnrollments = List.of();

        initializeUI();
        loadEnrollments(Term.MONSOON, 2025);
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

        JButton loadButton = new JButton("Load My Registrations");
        loadButton.addActionListener(e -> loadEnrollments());
        ButtonStyler.stylePrimary(loadButton);
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: enrollments table
        enrollmentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        enrollmentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.addActionListener(e -> dropSection());
        ButtonStyler.stylePrimary(dropButton);
        bottomPanel.add(dropButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadEnrollments());
        ButtonStyler.stylePrimary(refreshButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEnrollments() {
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            loadEnrollments(term, year);
        }
    }

    private void loadEnrollments(Term term, int year) {
        try {
            var student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Student not found"));
            currentEnrollments = enrollmentRepository.findByStudent(student.id());
            updateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading enrollments: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        Term selectedTerm = (Term) termCombo.getSelectedItem();
        Integer selectedYear = (Integer) yearCombo.getSelectedItem();
        
        for (Enrollment enrollment : currentEnrollments) {
            if (enrollment.status() != EnrollmentStatus.ENROLLED) {
                continue;
            }
            
            // Get section details
            Section section = sectionRepository.findById(enrollment.sectionId())
                    .orElse(null);
            if (section == null) {
                continue;
            }
            
            // Filter by selected term and year
            if (selectedTerm != null && selectedYear != null) {
                if (section.term() != selectedTerm || section.year() != selectedYear) {
                    continue;
                }
            }
            
            // Get course details
            Course course = courseRepository.findById(section.courseId())
                    .orElse(null);
            String courseName = course != null ? course.title() : "Unknown Course";
            String sectionCode = section.sectionCode();
            
            tableModel.addRow(new Object[]{
                    courseName,
                    sectionCode,
                    enrollment.status().name(),
                    enrollment.registeredAt() != null ? enrollment.registeredAt().toString() : "N/A",
                    "Drop"
            });
        }
    }

    private void dropSection() {
        int selectedRow = enrollmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an enrollment to drop.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Enrollment enrollment = currentEnrollments.get(selectedRow);
        if (enrollment.status() != EnrollmentStatus.ENROLLED) {
            JOptionPane.showMessageDialog(this, "This section is not currently enrolled.",
                    "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Drop this section? This action cannot be undone.",
                "Confirm Drop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            var result = studentApi.dropSection(userId, enrollment.id());
            if (result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadEnrollments(); // Refresh
            } else {
                JOptionPane.showMessageDialog(this, result.message(), "Drop Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

