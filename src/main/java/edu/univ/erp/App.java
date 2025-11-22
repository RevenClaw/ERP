package edu.univ.erp;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.ui.ApplicationLauncher;

/**
 * Main application entry point for the University ERP system.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        // Ensure UI runs on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Initializing application...");
                
                // Initialize application context (loads config, creates repositories, services, APIs)
                ApplicationContext context = new ApplicationContext();
                System.out.println("Application context initialized successfully.");

                // Launch the UI
                ApplicationLauncher launcher = new ApplicationLauncher(context);
                launcher.launch();
                System.out.println("UI launched. Login dialog should appear.");
            } catch (Exception ex) {
                System.err.println("Failed to start application: " + ex.getMessage());
                ex.printStackTrace();
                
                // Show error dialog if possible
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Failed to start application:\n" + ex.getMessage() + 
                            "\n\nCheck console for details.\n\nCommon issues:\n" +
                            "- MySQL not running\n" +
                            "- Database credentials incorrect\n" +
                            "- Databases not created",
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE);
                });
                System.exit(1);
            }
        });
    }
}

