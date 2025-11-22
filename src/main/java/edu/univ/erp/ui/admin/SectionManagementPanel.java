package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.Weekday;

/**
 * Panel for managing sections.
 */
public class SectionManagementPanel extends JPanel {

    private final AdminApi adminApi;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final JTable sectionsTable;
    private final DefaultTableModel tableModel;

    public SectionManagementPanel(ApplicationContext context) {
        this.adminApi = context.adminApi();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.instructorRepository = context.repositoryFactory().instructorRepository();
        this.tableModel = new DefaultTableModel(new String[]{
                "Course", "Section", "Term", "Year", "Day", "Time", "Room", "Capacity", "Instructor"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.sectionsTable = new JTable(tableModel);

        initializeUI();
        loadSections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top: action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Create Section");
        createButton.addActionListener(e -> showCreateSectionDialog());
        topPanel.add(createButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSections());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: sections table
        sectionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSections() {
        tableModel.setRowCount(0);
        try {
            for (Section section : adminApi.getAllSections()) {
                Course course = courseRepository.findById(section.courseId()).orElse(null);
                Instructor instructor = instructorRepository.findById(section.instructorId()).orElse(null);
                tableModel.addRow(new Object[]{
                        course != null ? course.code() : "N/A",
                        section.sectionCode(),
                        section.term().name(),
                        section.year(),
                        section.dayOfWeek().name(),
                        section.startTime() + " - " + section.endTime(),
                        section.room(),
                        section.capacity(),
                        instructor != null ? instructor.department() : "N/A"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateSectionDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Create Section", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Course selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        JComboBox<Course> courseCombo = new JComboBox<>();
        try {
            for (Course course : adminApi.getAllCourses()) {
                courseCombo.addItem(course);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        panel.add(courseCombo, gbc);

        // Instructor selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        JComboBox<Instructor> instructorCombo = new JComboBox<>();
        try {
            for (Instructor instructor : adminApi.getAllInstructors()) {
                instructorCombo.addItem(instructor);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading instructors: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        panel.add(instructorCombo, gbc);

        // Term
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Term:"), gbc);
        gbc.gridx = 1;
        JComboBox<Term> termCombo = new JComboBox<>(Term.values());
        panel.add(termCombo, gbc);

        // Year
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));
        panel.add(yearSpinner, gbc);

        // Section code
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Section Code:"), gbc);
        gbc.gridx = 1;
        JTextField sectionCodeField = new JTextField(20);
        panel.add(sectionCodeField, gbc);

        // Day of week
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1;
        JComboBox<Weekday> dayCombo = new JComboBox<>(Weekday.values());
        panel.add(dayCombo, gbc);

        // Start time
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Start Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        JTextField startTimeField = new JTextField(20);
        startTimeField.setToolTipText("Format: HH:MM (e.g., 10:00)");
        panel.add(startTimeField, gbc);

        // End time
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("End Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        JTextField endTimeField = new JTextField(20);
        endTimeField.setToolTipText("Format: HH:MM (e.g., 11:30)");
        panel.add(endTimeField, gbc);

        // Room
        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        JTextField roomField = new JTextField(20);
        panel.add(roomField, gbc);

        // Capacity
        gbc.gridx = 0;
        gbc.gridy = 9;
        panel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
        panel.add(capacitySpinner, gbc);

        // Enrollment deadline
        gbc.gridx = 0;
        gbc.gridy = 10;
        panel.add(new JLabel("Enrollment Deadline (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField deadlineField = new JTextField(20);
        deadlineField.setToolTipText("Format: YYYY-MM-DD (e.g., 2025-08-20)");
        panel.add(deadlineField, gbc);

        // Create button
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                Course course = (Course) courseCombo.getSelectedItem();
                Instructor instructor = (Instructor) instructorCombo.getSelectedItem();
                Term term = (Term) termCombo.getSelectedItem();
                int year = (Integer) yearSpinner.getValue();
                String sectionCode = sectionCodeField.getText().trim();
                Weekday day = (Weekday) dayCombo.getSelectedItem();
                LocalTime startTime = LocalTime.parse(startTimeField.getText().trim());
                LocalTime endTime = LocalTime.parse(endTimeField.getText().trim());
                String room = roomField.getText().trim();
                int capacity = (Integer) capacitySpinner.getValue();
                LocalDate deadline = LocalDate.parse(deadlineField.getText().trim());

                if (course == null || instructor == null || sectionCode.isEmpty() || room.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                var result = adminApi.createSection(course.id(), instructor.id(), term, year,
                        sectionCode, day, startTime, endTime, room, capacity, deadline);
                if (result.success()) {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadSections();
                } else {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(createButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}

