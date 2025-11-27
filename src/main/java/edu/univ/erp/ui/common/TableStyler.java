package edu.univ.erp.ui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Utility for applying a consistent, modern style to data tables.
 */
public final class TableStyler {

    private static final Color HEADER_BG = new Color(33, 78, 165);
    private static final Color HEADER_FG = Color.WHITE;
    private static final Color ROW_BG = Color.WHITE;
    private static final Color ALT_ROW_BG = new Color(247, 249, 253);
    private static final Color TEXT_COLOR = new Color(39, 51, 80);
    private static final Color SELECT_BG = new Color(213, 227, 255);
    private static final Color SELECT_FG = new Color(25, 42, 86);

    private TableStyler() {
    }

    public static void apply(JTable table) {
        table.setRowHeight(30);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(ROW_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(SELECT_FG);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setGridColor(new Color(230, 235, 245));

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(HEADER_BG);
            header.setForeground(HEADER_FG);
            header.setReorderingAllowed(false);
            Font headerFont = header.getFont();
            if (headerFont != null) {
                header.setFont(headerFont.deriveFont(Font.BOLD, 13f));
            }
            header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (component instanceof javax.swing.JComponent jComponent) {
                    jComponent.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                }

                if (!isSelected) {
                    component.setBackground(row % 2 == 0 ? ROW_BG : ALT_ROW_BG);
                    component.setForeground(TEXT_COLOR);
                }
                return component;
            }
        };

        table.setDefaultRenderer(Object.class, stripedRenderer);
        table.setDefaultRenderer(Number.class, stripedRenderer);
    }
}

