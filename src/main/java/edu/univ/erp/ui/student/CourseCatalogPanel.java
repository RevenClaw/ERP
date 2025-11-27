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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.catalog.CatalogApi;
import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.domain.Term;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.TableStyler;

/**
 * Panel for browsing course catalog and registering for sections.
 */
public class CourseCatalogPanel extends JPanel {

    private final long userId;
    private final CatalogApi catalogApi;
    private final StudentApi studentApi;
    private final JTable sectionsTable;
    private final DefaultTableModel tableModel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;
    private JTextField searchField;
    private List<SectionRow> currentSections;

    public CourseCatalogPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.catalogApi = context.catalogApi();
        this.studentApi = context.studentApi();
        this.tableModel = new DefaultTableModel(new String[]{
                "Course Code", "Course Title", "Section", "Day", "Time", "Room",
                "Instructor", "Available", "Deadline", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.sectionsTable = new JTable(tableModel);
        TableStyler.apply(sectionsTable);
        this.currentSections = List.of();

        initializeUI();
        loadSections(Term.MONSOON, 2025);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel: filters and search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Term:"));
        termCombo = new JComboBox<>(Term.values());
        termCombo.setSelectedItem(Term.MONSOON);
        termCombo.addActionListener(e -> loadSections()); // Auto-load on change
        topPanel.add(termCombo);

        topPanel.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        yearCombo.setSelectedItem(2025);
        yearCombo.addActionListener(e -> loadSections()); // Auto-load on change
        topPanel.add(yearCombo);

        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> performSearch()); // Search on Enter
        topPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        ButtonStyler.stylePrimary(searchButton);
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: sections table
        sectionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        sectionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton registerButton = new JButton("Register for Selected Section");
        registerButton.addActionListener(e -> registerForSection());
        ButtonStyler.stylePrimary(registerButton);
        bottomPanel.add(registerButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSections());
        ButtonStyler.stylePrimary(refreshButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
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
            currentSections = catalogApi.getSectionsForTerm(term, year, userId);
            System.out.println("Loaded " + currentSections.size() + " sections for " + term + " " + year);
            updateTable();
            if (currentSections.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "No sections found for " + term + " " + year + ".\n" +
                        "Please check that sections exist in the database for this term and year.",
                        "No Data", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Print full stack trace to console
            JOptionPane.showMessageDialog(this, 
                    "Error loading sections: " + ex.getMessage() + "\n" +
                    "Check console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadSections(); // If search is empty, show all
            return;
        }

        // Filter sections by keyword in course code or title
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            try {
                List<SectionRow> allSections = catalogApi.getSectionsForTerm(term, year, userId);
                currentSections = allSections.stream()
                        .filter(s -> s.courseCode().toLowerCase().contains(keyword) ||
                                   s.courseTitle().toLowerCase().contains(keyword))
                        .collect(java.util.stream.Collectors.toList());
                updateTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error searching sections: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        java.time.LocalDate today = java.time.LocalDate.now();
        for (SectionRow section : currentSections) {
            String status;
            if (section.isEnrolled()) {
                status = "Enrolled";
            } else if (section.enrollmentDeadline().isBefore(today)) {
                status = "Unavailable (Deadline Passed)";
            } else if (section.availableSeats() <= 0) {
                status = "Full";
            } else {
                status = "Available";
            }
            tableModel.addRow(new Object[]{
                    section.courseCode(),
                    section.courseTitle(),
                    section.sectionCode(),
                    section.dayOfWeek().name(),
                    section.startTime() + " - " + section.endTime(),
                    section.room(),
                    section.instructorName(),
                    section.availableSeats() + " / " + section.capacity(),
                    section.enrollmentDeadline().toString(),
                    status
            });
        }
    }

    private void registerForSection() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a section to register.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SectionRow section = currentSections.get(selectedRow);
        java.time.LocalDate today = java.time.LocalDate.now();
        
        if (section.isEnrolled()) {
            JOptionPane.showMessageDialog(this, "You are already enrolled in this section.",
                    "Already Enrolled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (section.enrollmentDeadline().isBefore(today)) {
            JOptionPane.showMessageDialog(this, "Enrollment deadline has passed for this section.",
                    "Deadline Passed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (section.availableSeats() <= 0) {
            JOptionPane.showMessageDialog(this, "This section is full. No available seats.",
                    "Section Full", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Register for " + section.courseCode() + " - " + section.sectionCode() + "?",
                "Confirm Registration", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            var result = studentApi.registerForSection(userId, section.sectionId());
            if (result.success()) {
                JOptionPane.showMessageDialog(this, result.message(), "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadSections(); // Refresh
            } else {
                JOptionPane.showMessageDialog(this, result.message(), "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

