package org.openjava.probe.client.gui;

import org.openjava.probe.client.gui.event.GuiEventMulticaster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CommandPanel extends JPanel {
    private final JTextField command;
    private final JButton sendButton;

    public CommandPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createEtchedBorder());
        setBackground(Color.LIGHT_GRAY);
        command = new JTextField();
        command.setPreferredSize(new Dimension(510, 40));
        command.setMargin(new Insets(0, 100, 0, 0));
        command.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    String commandText = command.getText();
                    if (commandText != null && commandText.trim().length() > 0) {
                        GuiEventMulticaster.getInstance().sendCommand(commandText);
                    }
                }
            }
        });
        add(command);
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String commandText = command.getText();
            if (commandText != null && commandText.trim().length() > 0) {
                GuiEventMulticaster.getInstance().sendCommand(commandText);
            }
        });
        sendButton.setPreferredSize(new Dimension(80, 40));
        add(sendButton);
        setEnabled(false);
    }

    public void clear() {
        command.setText("");
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        sendButton.setEnabled(b);
        command.setEnabled(b);
    }
}
