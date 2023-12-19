package org.openjava.probe.client.gui;

import javax.swing.*;
import java.awt.*;

public class CommandPanel extends JPanel {
    private static CommandPanel instance = new CommandPanel();

    private CommandPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createEtchedBorder());
        setBackground(Color.LIGHT_GRAY);
        JTextField command = new JTextField();
        command.setPreferredSize(new Dimension(510, 40));
        add(command);
        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 40));
        add(sendButton);
    }

    public static CommandPanel getInstance() {
        return instance;
    }
}
