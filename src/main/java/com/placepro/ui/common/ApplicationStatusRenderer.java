package com.placepro.ui.common;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Anti-Aliased Pill Badge Cell Renderer for Application & Drive Statuses.
 */
public final class ApplicationStatusRenderer extends javax.swing.JPanel implements TableCellRenderer {

    private String text = "";
    private Color bgPillColor = UiStyles.PRIMARY_SOFT_COLOR;
    private Color textPillColor = UiStyles.PRIMARY_COLOR;
    private boolean isSelected = false;

    public ApplicationStatusRenderer() {
        setOpaque(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        this.isSelected = isSelected;
        if (value != null) {
            this.text = value.toString().replace("_", " ");
            applyStyle(value.toString());
        } else {
            this.text = "-";
            this.bgPillColor = UiStyles.SURFACE_ALT_COLOR;
            this.textPillColor = UiStyles.MUTED_TEXT_COLOR;
        }
        return this;
    }

    private void applyStyle(String status) {
        String s = status.toUpperCase();
        if (s.contains("SELECTED") || s.contains("HIRED") || s.contains("OFFERED") || s.contains("PUBLISHED")) {
            bgPillColor = UiStyles.SUCCESS_SOFT_COLOR;
            textPillColor = new Color(4, 120, 87); // Emerald dark
        } else if (s.contains("SHORTLISTED") || s.contains("INTERVIEW") || s.contains("SCHEDULED") || s.contains("PENDING")) {
            bgPillColor = UiStyles.WARNING_SOFT_COLOR;
            textPillColor = new Color(180, 83, 9); // Amber dark
        } else if (s.contains("REJECTED") || s.contains("CLOSED") || s.contains("CANCELLED")) {
            bgPillColor = UiStyles.ERROR_SOFT_COLOR;
            textPillColor = new Color(185, 28, 28); // Red dark
        } else if (s.contains("APPLIED") || s.contains("ACTIVE") || s.contains("OPEN")) {
            bgPillColor = UiStyles.PRIMARY_SOFT_COLOR;
            textPillColor = UiStyles.PRIMARY_DARK_COLOR;
        } else {
            bgPillColor = UiStyles.SURFACE_ALT_COLOR;
            textPillColor = UiStyles.MUTED_TEXT_COLOR;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (isSelected) {
            g2.setColor(UiStyles.PRIMARY_SOFT_COLOR);
            g2.fillRect(0, 0, w, h);
        }

        int pillWidth = Math.min(w - 12, 140);
        int pillHeight = Math.min(h - 8, 24);
        int x = (w - pillWidth) / 2;
        int y = (h - pillHeight) / 2;

        g2.setColor(bgPillColor);
        g2.fill(new RoundRectangle2D.Float(x, y, pillWidth, pillHeight, 14, 14));

        g2.setColor(textPillColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int stringW = fm.stringWidth(text);
        int stringY = y + ((pillHeight - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(text, x + (pillWidth - stringW) / 2, stringY);
        g2.dispose();
    }

    public static Color colorForStatus(String status) {
        if (status == null) return UiStyles.MUTED_TEXT_COLOR;
        String s = status.toUpperCase();
        if (s.contains("SELECTED") || s.contains("PUBLISHED")) return UiStyles.SUCCESS_COLOR;
        if (s.contains("SHORTLISTED") || s.contains("INTERVIEW")) return UiStyles.WARNING_COLOR;
        if (s.contains("REJECTED")) return UiStyles.ERROR_COLOR;
        return UiStyles.PRIMARY_COLOR;
    }
}
