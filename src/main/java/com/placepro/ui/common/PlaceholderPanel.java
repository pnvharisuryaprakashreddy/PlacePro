package com.placepro.ui.common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String message) {
        setLayout(new BorderLayout());
        add(new JLabel(message, JLabel.CENTER), BorderLayout.CENTER);
    }
}
