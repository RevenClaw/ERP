package edu.univ.erp.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import edu.univ.erp.api.types.LoginResponse;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.auth.LoginDialog;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

/**
 * Launches the application UI and handles navigation between screens.
 */
public final class ApplicationLauncher {

    private final ApplicationContext context;

    public ApplicationLauncher(ApplicationContext context) {
        this.context = context;
    }

    public void launch() {
        // Set up modern look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            System.out.println("FlatLaf look and feel set successfully.");
        } catch (Exception ex) {
            System.err.println("Failed to set FlatLaf look and feel: " + ex.getMessage());
            ex.printStackTrace();
            // Continue with default look and feel
        }

        // Show login dialog (we're already on EDT, but ensure it)
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Creating login dialog...");
                LoginDialog loginDialog = new LoginDialog(
                        context.authApi(),
                        this::handleLoginSuccess);
                System.out.println("Showing login dialog...");
                loginDialog.showDialog();
                System.out.println("Login dialog displayed.");
            } catch (Exception ex) {
                System.err.println("Failed to show login dialog: " + ex.getMessage());
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Failed to show login dialog:\n" + ex.getMessage(),
                        "UI Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        boolean maintenanceMode = response.maintenanceMode();
        String maintenanceMessage = context.config().maintenanceBanner();

        switch (response.role()) {
            case STUDENT:
                StudentDashboard studentDashboard = new StudentDashboard(
                        response, maintenanceMode, maintenanceMessage, context);
                studentDashboard.setVisible(true);
                break;

            case INSTRUCTOR:
                InstructorDashboard instructorDashboard = new InstructorDashboard(
                        response, maintenanceMode, maintenanceMessage, context);
                instructorDashboard.setVisible(true);
                break;

            case ADMIN:
                AdminDashboard adminDashboard = new AdminDashboard(
                        response, maintenanceMode, maintenanceMessage, context);
                adminDashboard.setVisible(true);
                break;

            default:
                throw new IllegalStateException("Unknown role: " + response.role());
        }
    }
}

