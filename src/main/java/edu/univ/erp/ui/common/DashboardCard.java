package edu.univ.erp.ui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Reusable dashboard card that changes color on hover/click and triggers an action.
 */
public final class DashboardCard extends JPanel {

    private static final Color BASE_COLOR = new Color(245, 247, 252);
    private static final Color HOVER_COLOR = new Color(233, 239, 255);
    private static final Color ACTIVE_COLOR = new Color(216, 228, 255);

    private final Runnable clickHandler;
    private boolean active;

    public DashboardCard(String title, String description, Runnable onClick) {
        this.clickHandler = onClick;
        setLayout(new BorderLayout(0, 6));
        setBorder(new EmptyBorder(18, 20, 18, 20));
        setPreferredSize(new java.awt.Dimension(240, 130));
        setMaximumSize(new java.awt.Dimension(260, 150));
        setBackground(BASE_COLOR);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(false);

        JLabel titleLabel = new JLabel(title);
        Font baseFont = titleLabel.getFont();
        titleLabel.setFont(baseFont.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(new Color(38, 50, 82));

        JLabel descriptionLabel = new JLabel(
                "<html><body style='width: 160px;'>" + description + "</body></html>");
        descriptionLabel.setFont(baseFont.deriveFont(Font.PLAIN, 13f));
        descriptionLabel.setForeground(new Color(96, 109, 139));

        add(titleLabel, BorderLayout.NORTH);
        add(descriptionLabel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshBackground();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickHandler != null) {
                    clickHandler.run();
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        refreshBackground();
    }

    private void refreshBackground() {
        setBackground(active ? ACTIVE_COLOR : BASE_COLOR);
    }
}

