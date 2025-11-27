package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.admin.AdminApi;
import edu.univ.erp.auth.AccountStatus;
import edu.univ.erp.auth.AuthUser;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.AuthUserRepository;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.ui.common.ButtonStyler;
import edu.univ.erp.ui.common.TableStyler;

/**
 * Panel for managing users (students and instructors).
 */
public class UserManagementPanel extends JPanel {

    private final AdminApi adminApi;
    private final AuthUserRepository authUserRepository;
    private final JTable usersTable;
    private final DefaultTableModel tableModel;
    private List<UserRow> userRows;

    public UserManagementPanel(ApplicationContext context) {
        this.adminApi = context.adminApi();
        this.authUserRepository = context.repositoryFactory().authUserRepository();
        this.tableModel = new DefaultTableModel(new String[]{
                "Type", "Username", "Role", "Status", "Roll No/Dept", "Program/Title", "Year/Notes"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.usersTable = new JTable(tableModel);
        TableStyler.apply(usersTable);
        this.userRows = new ArrayList<>();

        initializeUI();
        loadUsers();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Center: users table
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton createStudentButton = new JButton("Create Student");
        createStudentButton.addActionListener(e -> showCreateStudentDialog());
        ButtonStyler.stylePrimary(createStudentButton);
        bottomPanel.add(createStudentButton);

        JButton createInstructorButton = new JButton("Create Instructor");
        createInstructorButton.addActionListener(e -> showCreateInstructorDialog());
        ButtonStyler.stylePrimary(createInstructorButton);
        bottomPanel.add(createInstructorButton);

        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editSelectedUser());
        ButtonStyler.stylePrimary(editButton);
        bottomPanel.add(editButton);

        JButton deactivateButton = new JButton("Deactivate User");
        deactivateButton.addActionListener(e -> deactivateSelectedUser());
        ButtonStyler.stylePrimary(deactivateButton);
        bottomPanel.add(deactivateButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsers());
        ButtonStyler.stylePrimary(refreshButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        userRows = new ArrayList<>();
        try {
            Map<Long, AuthUser> authUsers = new HashMap<>();
            for (AuthUser user : authUserRepository.findAll()) {
                authUsers.put(user.id(), user);
            }
            Set<Long> listedUserIds = new HashSet<>();

            for (Student student : adminApi.getAllStudents()) {
                AuthUser authUser = authUsers.get(student.userId());
                String username = authUser != null ? authUser.username() : "[Not Linked]";
                UserRole role = authUser != null ? authUser.role() : UserRole.STUDENT;
                AccountStatus status = authUser != null ? authUser.status() : AccountStatus.DISABLED;
                userRows.add(new UserRow(student.userId(), "Student", role, status, username, student, null));
                listedUserIds.add(student.userId());
                tableModel.addRow(new Object[]{
                        "Student",
                        username,
                        role.name(),
                        status.name(),
                        student.rollNumber(),
                        student.program(),
                        "Year " + student.yearOfStudy()
                });
            }

            for (Instructor instructor : adminApi.getAllInstructors()) {
                AuthUser authUser = authUsers.get(instructor.userId());
                String username = authUser != null ? authUser.username() : "[Not Linked]";
                UserRole role = authUser != null ? authUser.role() : UserRole.INSTRUCTOR;
                AccountStatus status = authUser != null ? authUser.status() : AccountStatus.DISABLED;
                userRows.add(new UserRow(instructor.userId(), "Instructor", role, status, username, null, instructor));
                listedUserIds.add(instructor.userId());
                tableModel.addRow(new Object[]{
                        "Instructor",
                        username,
                        role.name(),
                        status.name(),
                        instructor.department(),
                        instructor.title(),
                        "Assigned"
                });
            }

            for (AuthUser authUser : authUsers.values()) {
                if (listedUserIds.contains(authUser.id())) {
                    continue;
                }
                userRows.add(new UserRow(authUser.id(), authUser.role().name(), authUser.role(),
                        authUser.status(), authUser.username(), null, null));
                tableModel.addRow(new Object[]{
                        authUser.role().name(),
                        authUser.username(),
                        authUser.role().name(),
                        authUser.status().name(),
                        "-",
                        "-",
                        "-"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedUser() {
        UserRow row = getSelectedUserRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showEditUserDialog(row);
    }

    private void deactivateSelectedUser() {
        UserRow row = getSelectedUserRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to deactivate.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Disable account for " + row.username + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        var result = adminApi.deactivateUser(row.userId);
        if (result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, result.message(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private UserRow getSelectedUserRow() {
        int index = usersTable.getSelectedRow();
        if (index < 0 || index >= userRows.size()) {
            return null;
        }
        return userRows.get(index);
    }

    private void showEditUserDialog(UserRow row) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Edit User", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(row.username), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        roleCombo.setSelectedItem(row.role);
        panel.add(roleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JComboBox<AccountStatus> statusCombo = new JComboBox<>(AccountStatus.values());
        statusCombo.setSelectedItem(row.status);
        panel.add(statusCombo, gbc);

        JPanel studentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints studentGbc = new GridBagConstraints();
        studentGbc.insets = new Insets(4, 4, 4, 4);
        studentGbc.anchor = GridBagConstraints.WEST;
        studentGbc.gridx = 0;
        studentGbc.gridy = 0;
        studentPanel.add(new JLabel("Roll No:"), studentGbc);
        studentGbc.gridx = 1;
        JTextField rollField = new JTextField(row.student != null ? row.student.rollNumber() : "", 15);
        studentPanel.add(rollField, studentGbc);
        studentGbc.gridx = 0;
        studentGbc.gridy = 1;
        studentPanel.add(new JLabel("Program:"), studentGbc);
        studentGbc.gridx = 1;
        JTextField programField = new JTextField(row.student != null ? row.student.program() : "", 15);
        studentPanel.add(programField, studentGbc);
        studentGbc.gridx = 0;
        studentGbc.gridy = 2;
        studentPanel.add(new JLabel("Year:"), studentGbc);
        studentGbc.gridx = 1;
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        if (row.student != null) {
            yearCombo.setSelectedItem(row.student.yearOfStudy());
        }
        studentPanel.add(yearCombo, studentGbc);

        JPanel instructorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints instGbc = new GridBagConstraints();
        instGbc.insets = new Insets(4, 4, 4, 4);
        instGbc.anchor = GridBagConstraints.WEST;
        instGbc.gridx = 0;
        instGbc.gridy = 0;
        instructorPanel.add(new JLabel("Department:"), instGbc);
        instGbc.gridx = 1;
        JTextField deptField = new JTextField(row.instructor != null ? row.instructor.department() : "", 15);
        instructorPanel.add(deptField, instGbc);
        instGbc.gridx = 0;
        instGbc.gridy = 1;
        instructorPanel.add(new JLabel("Title:"), instGbc);
        instGbc.gridx = 1;
        JTextField titleField = new JTextField(row.instructor != null ? row.instructor.title() : "", 15);
        instructorPanel.add(titleField, instGbc);

        CardLayout roleCards = new CardLayout();
        JPanel roleSpecificPanel = new JPanel(roleCards);
        roleSpecificPanel.add(studentPanel, UserRole.STUDENT.name());
        roleSpecificPanel.add(instructorPanel, UserRole.INSTRUCTOR.name());
        roleSpecificPanel.add(new JPanel(), UserRole.ADMIN.name());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(roleSpecificPanel, gbc);

        roleCards.show(roleSpecificPanel, row.role.name());
        roleCombo.addActionListener(e -> {
            UserRole selected = (UserRole) roleCombo.getSelectedItem();
            roleCards.show(roleSpecificPanel, selected.name());
        });

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                UserRole selectedRole = (UserRole) roleCombo.getSelectedItem();
                AccountStatus selectedStatus = (AccountStatus) statusCombo.getSelectedItem();
                AdminApi.StudentProfilePayload studentPayload = null;
                AdminApi.InstructorProfilePayload instructorPayload = null;

                if (selectedRole == UserRole.STUDENT) {
                    String roll = rollField.getText().trim();
                    String program = programField.getText().trim();
                    if (roll.isEmpty() || program.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Roll number and program are required for students.",
                                "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int year = (Integer) yearCombo.getSelectedItem();
                    studentPayload = new AdminApi.StudentProfilePayload(roll, program, year);
                } else if (selectedRole == UserRole.INSTRUCTOR) {
                    String dept = deptField.getText().trim();
                    String title = titleField.getText().trim();
                    if (dept.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Department is required for instructors.",
                                "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    instructorPayload = new AdminApi.InstructorProfilePayload(dept, title);
                }

                var result = adminApi.updateUser(row.userId, selectedRole, selectedStatus,
                        studentPayload, instructorPayload);
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
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveButton, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
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

    private record UserRow(long userId,
                           String type,
                           UserRole role,
                           AccountStatus status,
                           String username,
                           Student student,
                           Instructor instructor) {
    }
}
