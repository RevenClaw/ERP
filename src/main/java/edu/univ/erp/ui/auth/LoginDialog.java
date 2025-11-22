package edu.univ.erp.ui.auth;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.univ.erp.api.auth.AuthApi;
import edu.univ.erp.api.types.LoginRequest;
import edu.univ.erp.api.types.LoginResponse;

/**
 * Login dialog for user authentication.
 */
public final class LoginDialog extends JDialog {

    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 200;

    private final AuthApi authApi;
    private final LoginCallback callback;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private boolean authenticated;

    public interface LoginCallback {
        void onLoginSuccess(LoginResponse response);
    }

    public LoginDialog(AuthApi authApi, LoginCallback callback) {
        this.authApi = authApi;
        this.callback = callback;
        this.authenticated = false;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("University ERP - Login");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!authenticated) {
                    System.exit(0);
                }
            }
        });

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.addActionListener(e -> performLogin());
        mainPanel.add(passwordField, gbc);

        // Buttons panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> performLogin());
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(e -> {
            authenticated = false;
            dispose();
            System.exit(0);
        });
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(null);

        // Set default button
        getRootPane().setDefaultButton(loginButton);

        // Focus on username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a username.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }

        // Disable buttons during login
        loginButton.setEnabled(false);
        cancelButton.setEnabled(false);

        // Perform login on background thread to avoid blocking UI
        new Thread(() -> {
            try {
                LoginRequest request = new LoginRequest(username, password);
                LoginResponse response = authApi.login(request);

                SwingUtilities.invokeLater(() -> {
                    if (response.success()) {
                        authenticated = true;
                        dispose();
                        callback.onLoginSuccess(response);
                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this,
                                response.message(),
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                        loginButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "An error occurred during login: " + ex.getMessage(),
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                    loginButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                });
            }
        }).start();
    }

    public void showDialog() {
        System.out.println("LoginDialog.showDialog() called");
        System.out.println("Dialog visible: " + isVisible());
        System.out.println("Dialog modal: " + isModal());
        setVisible(true);
        System.out.println("Dialog setVisible(true) called");
        System.out.println("Dialog visible after: " + isVisible());
        toFront();
        requestFocus();
    }
}

