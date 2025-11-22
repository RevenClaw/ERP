package edu.univ.erp.ui.student;

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

import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;

/**
 * Panel for displaying student timetable.
 */
public class TimetablePanel extends JPanel {

    private final long userId;
    private final StudentApi studentApi;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final JTable timetableTable;
    private final DefaultTableModel tableModel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;

    public TimetablePanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.studentApi = context.studentApi();
        this.studentRepository = context.repositoryFactory().studentRepository();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.instructorRepository = context.repositoryFactory().instructorRepository();
        this.tableModel = new DefaultTableModel(new String[]{
                "Course", "Section", "Day", "Time", "Room", "Instructor"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.timetableTable = new JTable(tableModel);

        initializeUI();
        loadTimetable(Term.MONSOON, 2025);
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

        javax.swing.JButton loadButton = new javax.swing.JButton("Load Timetable");
        loadButton.addActionListener(e -> loadTimetable());
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: timetable table
        timetableTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTimetable() {
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            loadTimetable(term, year);
        }
    }

    private void loadTimetable(Term term, int year) {
        try {
            studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Student not found"));
            List<Section> sections = studentApi.getTimetable(userId, term, year);
            updateTable(sections);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Section> sections) {
        tableModel.setRowCount(0);
        for (Section section : sections) {
            // Get course details
            Course course = courseRepository.findById(section.courseId())
                    .orElse(null);
            String courseName = course != null ? course.title() : "Unknown Course";
            
            // Get instructor details
            Instructor instructor = instructorRepository.findById(section.instructorId())
                    .orElse(null);
            String instructorName = instructor != null 
                    ? instructor.title() + " " + instructor.department()
                    : "TBA";
            
            tableModel.addRow(new Object[]{
                    courseName,
                    section.sectionCode(),
                    section.dayOfWeek().name(),
                    section.startTime() + " - " + section.endTime(),
                    section.room(),
                    instructorName
            });
        }
    }
}

