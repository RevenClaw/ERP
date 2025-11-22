package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.domain.Course;

/**
 * Panel for managing courses.
 */
public class CourseManagementPanel extends JPanel {

    private final AdminApi adminApi;
    private final JTable coursesTable;
    private final DefaultTableModel tableModel;

    public CourseManagementPanel(ApplicationContext context) {
        this.adminApi = context.adminApi();
        this.tableModel = new DefaultTableModel(new String[]{
                "Code", "Title", "Credits", "Description"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.coursesTable = new JTable(tableModel);

        initializeUI();
        loadCourses();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top: action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Create Course");
        createButton.addActionListener(e -> showCreateCourseDialog());
        topPanel.add(createButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: courses table
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        try {
            for (Course course : adminApi.getAllCourses()) {
                tableModel.addRow(new Object[]{
                        course.code(),
                        course.title(),
                        course.credits(),
                        course.description()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateCourseDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Create Course", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        JTextField codeField = new JTextField(20);
        panel.add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        JTextField creditsField = new JTextField(20);
        panel.add(creditsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        panel.add(descScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String title = titleField.getText().trim();
                String creditsStr = creditsField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Code, title, and credits are required.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double credits = Double.parseDouble(creditsStr);
                if (credits <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Credits must be greater than 0.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                var result = adminApi.createCourse(code, title, credits, description);
                if (result.success()) {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid credits format.", "Error",
                        JOptionPane.ERROR_MESSAGE);
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

