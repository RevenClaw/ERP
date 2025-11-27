package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.AuthRepository;
import edu.univ.erp.domain.AuthUser;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;

/**
 * Panel for managing users (students and instructors).
 */
public class UserManagementPanel extends JPanel {

    private final AdminApi adminApi;
    private final AuthRepository authRepository;
    private final JTable usersTable;
    private final DefaultTableModel tableModel;

    public UserManagementPanel(ApplicationContext context) {
        this.adminApi = context.adminApi();
        this.authRepository = context.repositoryFactory().authRepository();
        this.tableModel = new DefaultTableModel(new String[]{
                "Type", "Username", "Roll No/Dept", "Program/Title", "Year/Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.usersTable = new JTable(tableModel);

        initializeUI();
        loadUsers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top: action buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createStudentButton = new JButton("Create Student");
        createStudentButton.addActionListener(e -> showCreateStudentDialog());
        topPanel.add(createStudentButton);

        JButton createInstructorButton = new JButton("Create Instructor");
        createInstructorButton.addActionListener(e -> showCreateInstructorDialog());
        topPanel.add(createInstructorButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsers());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: users table
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            // Fetch all auth users into a map for efficient lookup
            Map<Long, AuthUser> authUserMap = authRepository.findAll().stream()
                    .collect(Collectors.toMap(AuthUser::id, Function.identity()));

            // Load students
            for (Student student : adminApi.getAllStudents()) {
                AuthUser authUser = authUserMap.get(student.userId());
                String username = (authUser != null) ? authUser.username() : "[Not Linked]";
                tableModel.addRow(new Object[]{
                        "Student",
                        username,
                        student.rollNumber(),
                        student.program(),
                        "Year " + student.yearOfStudy()
                });
            }

            // Load instructors
            for (Instructor instructor : adminApi.getAllInstructors()) {
                AuthUser authUser = authUserMap.get(instructor.userId());
                String username = (authUser != null) ? authUser.username() : "[Not Linked]";
                tableModel.addRow(new Object[]{
                        "Instructor",
                        username,
                        instructor.department(),
                        instructor.title(),
                        "Active"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateStudentDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Create Student", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Roll Number:"), gbc);
        gbc.gridx = 1;
        JTextField rollNumberField = new JTextField(20);
        panel.add(rollNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Program:"), gbc);
        gbc.gridx = 1;
        JTextField programField = new JTextField(20);
        panel.add(programField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Year of Study:"), gbc);
        gbc.gridx = 1;
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        panel.add(yearCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String rollNumber = rollNumberField.getText().trim();
                String program = programField.getText().trim();
                int year = (Integer) yearCombo.getSelectedItem();

                if (username.isEmpty() || password.isEmpty() || rollNumber.isEmpty() || program.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                var result = adminApi.createStudent(username, password, rollNumber, program, year);
                if (result.success()) {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUsers();
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

    private void showCreateInstructorDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Create Instructor", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        JTextField departmentField = new JTextField(20);
        panel.add(departmentField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String department = departmentField.getText().trim();
                String title = titleField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || department.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Username, password, and department are required.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                var result = adminApi.createInstructor(username, password, department, title);
                if (result.success()) {
                    JOptionPane.showMessageDialog(dialog, result.message(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUsers();
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
