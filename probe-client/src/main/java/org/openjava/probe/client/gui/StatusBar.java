package org.openjava.probe.client.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel statusLabel;

    public StatusBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
        setBackground(Color.LIGHT_GRAY);
        statusLabel = new JLabel("status bar");
        add(statusLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}
