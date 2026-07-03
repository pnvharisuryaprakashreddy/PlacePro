package com.placepro.ui.common;

import com.placepro.service.application.ApplicationStatus;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public final class ApplicationStatusRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        if (!(component instanceof JLabel) || value == null) {
            return component;
        }
        JLabel label = (JLabel) component;
        label.setOpaque(true);
        Color statusColor = colorForStatus(value.toString());
        // Darken the badge when selected so selection stays visible without losing the status color.
        label.setBackground(isSelected ? statusColor.darker() : statusColor);
        label.setForeground(Color.WHITE);
        return label;
    }

    public static Color colorForStatus(String status) {
        try {
            switch (ApplicationStatus.valueOf(status)) {
                case APPLIED:
                    return new Color(41, 98, 255);
                case SHORTLISTED:
                    return new Color(0, 150, 136);
                case INTERVIEW_SCHEDULED:
                    return new Color(255, 143, 0);
                case SELECTED:
                    return new Color(46, 125, 50);
                case REJECTED:
                    return new Color(198, 40, 40);
                case ON_HOLD:
                    return new Color(117, 117, 117);
                default:
                    return new Color(96, 96, 96);
            }
        } catch (IllegalArgumentException exception) {
            return new Color(96, 96, 96);
        }
    }
}
