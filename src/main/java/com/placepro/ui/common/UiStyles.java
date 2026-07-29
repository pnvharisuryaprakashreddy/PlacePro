package com.placepro.ui.common;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-Enterprise UI Design System & Java 2D Graphics Engine for PlacePro.
 */
public final class UiStyles {

    // Ultra-Enterprise Palette (Slate Dark, Indigo Primary, Emerald Success, Amber Warning, Crimson Error)
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252);     // #F8FAFC
    public static final Color SURFACE_COLOR = Color.WHITE;                      // #FFFFFF
    public static final Color SURFACE_ALT_COLOR = new Color(241, 245, 249);  // #F1F5F9
    public static final Color BORDER_COLOR = new Color(226, 232, 240);       // #E2E8F0
    public static final Color MUTED_BORDER_COLOR = new Color(241, 245, 249); // #F1F5F9
    public static final Color TEXT_COLOR = new Color(15, 23, 42);            // #0F172A
    public static final Color MUTED_TEXT_COLOR = new Color(100, 116, 139);   // #64748B
    
    public static final Color PRIMARY_COLOR = new Color(79, 70, 229);        // #4F46E5 Indigo
    public static final Color PRIMARY_HOVER_COLOR = new Color(67, 56, 202);  // #4338CA Dark Indigo
    public static final Color PRIMARY_DARK_COLOR = new Color(49, 46, 129);   // #312E81 Deep Indigo
    public static final Color PRIMARY_SOFT_COLOR = new Color(238, 242, 255);  // #EEF2FF Soft Indigo Tint
    
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);       // #10B981 Emerald
    public static final Color SUCCESS_SOFT_COLOR = new Color(209, 250, 229);  // #D1FAE5
    public static final Color WARNING_COLOR = new Color(245, 158, 11);       // #F59E0B Amber
    public static final Color WARNING_SOFT_COLOR = new Color(254, 243, 199);  // #FEF3C7
    public static final Color ERROR_COLOR = new Color(239, 68, 68);          // #EF4444 Crimson Red
    public static final Color ERROR_SOFT_COLOR = new Color(254, 226, 226);    // #FEE2E2

    public static final Color HEADER_BG_START = new Color(15, 23, 42);       // Dark Slate 900
    public static final Color HEADER_BG_END = new Color(30, 41, 59);         // Slate 800

    // Typography
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font KPI_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private UiStyles() {
    }

    public static JPanel createPagePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        return panel;
    }

    public static JPanel createSurfacePanel() {
        RoundedPanel panel = new RoundedPanel(16, SURFACE_COLOR, BORDER_COLOR);
        panel.setLayout(new java.awt.BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    public static JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(14, SURFACE_COLOR, BORDER_COLOR);
        panel.setLayout(new java.awt.BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    public static JPanel createToolbarPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return panel;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(MUTED_TEXT_COLOR);
        return label;
    }

    public static JLabel createStatusLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(BODY_FONT);
        label.setForeground(MUTED_TEXT_COLOR);
        return label;
    }

    public static JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(BODY_FONT);
        label.setForeground(ERROR_COLOR);
        return label;
    }

    public static JLabel createKpiValueLabel() {
        JLabel label = new JLabel("-", SwingConstants.LEFT);
        label.setFont(KPI_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    public static JButton stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY_COLOR, Color.WHITE, PRIMARY_HOVER_COLOR);
        return button;
    }

    public static JButton styleSecondaryButton(JButton button) {
        styleButton(button, SURFACE_ALT_COLOR, TEXT_COLOR, BORDER_COLOR);
        return button;
    }

    public static JButton styleDangerButton(JButton button) {
        styleButton(button, ERROR_COLOR, Color.WHITE, new Color(220, 38, 38));
        return button;
    }

    private static void styleButton(JButton button, Color background, Color foreground, Color hoverBackground) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setForeground(foreground);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            private boolean isHover = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
                button.repaint();
            }
        });

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isHover = button.getModel().isRollover();
                boolean isPressed = button.getModel().isPressed();
                Color currentBg = isPressed ? hoverBackground.darker() : (isHover ? hoverBackground : background);

                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 12, 12));

                if (background.equals(SURFACE_ALT_COLOR)) {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, c.getWidth() - 1, c.getHeight() - 1, 12, 12));
                }

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    public static void styleInput(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(SURFACE_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(BODY_FONT);
        area.setForeground(TEXT_COLOR);
        area.setBackground(SURFACE_COLOR);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(BODY_FONT);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBackground(SURFACE_COLOR);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    public static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(BODY_FONT);
        checkBox.setOpaque(false);
        checkBox.setForeground(TEXT_COLOR);
        checkBox.setFocusPainted(false);
    }

    public static void styleLabel(JLabel label) {
        label.setFont(BODY_FONT);
        label.setForeground(TEXT_COLOR);
    }

    public static JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.getViewport().setBackground(SURFACE_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return scrollPane;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(TEXT_COLOR);
        table.setBackground(SURFACE_COLOR);
        table.setSelectionBackground(PRIMARY_SOFT_COLOR);
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(MUTED_BORDER_COLOR);
        table.setRowHeight(34);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(SURFACE_ALT_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);

        // Zebra striping cell renderer fallback if not custom
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? SURFACE_COLOR : SURFACE_ALT_COLOR);
                }
                c.setForeground(TEXT_COLOR);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(BACKGROUND_COLOR);
        tabs.setForeground(TEXT_COLOR);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.contentAreaColor", SURFACE_COLOR);
        UIManager.put("TabbedPane.selected", SURFACE_COLOR);
        UIManager.put("TabbedPane.focus", PRIMARY_COLOR);
    }

    public static JLabel createLinkLabel(String text, Runnable action) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setFont(BODY_FONT);
        label.setForeground(PRIMARY_COLOR);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.run();
            }
        });
        return label;
    }

    public static void applySurface(JComponent component) {
        component.setBackground(SURFACE_COLOR);
        component.setForeground(TEXT_COLOR);
        component.setFont(BODY_FONT);
    }

    /**
     * Anti-aliased Custom Panel with Smooth Rounded Rectangles and Subtle Border.
     */
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;
        private final Color strokeColor;

        public RoundedPanel(int radius, Color bg, Color stroke) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bg;
            this.strokeColor = stroke;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));

            if (strokeColor != null) {
                g2.setColor(strokeColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, cornerRadius, cornerRadius));
            }

            g2.dispose();
        }
    }

    /**
     * Slate Dark Header Panel with subtle gradient background.
     */
    public static class DarkHeaderPanel extends JPanel {
        public DarkHeaderPanel() {
            super();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(0, 0, HEADER_BG_START, getWidth(), 0, HEADER_BG_END);
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(51, 65, 85)); // Slate border line
            g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
