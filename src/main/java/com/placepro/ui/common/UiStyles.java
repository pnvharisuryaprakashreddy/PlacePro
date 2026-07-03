package com.placepro.ui.common;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UiStyles {

    public static final Color ERROR_COLOR = new Color(180, 0, 0);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);

    private UiStyles() {
    }

    public static JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ERROR_COLOR);
        return label;
    }

    public static JLabel createLinkLabel(String text, Runnable action) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(new Color(0, 102, 204));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.run();
            }
        });
        return label;
    }
}
