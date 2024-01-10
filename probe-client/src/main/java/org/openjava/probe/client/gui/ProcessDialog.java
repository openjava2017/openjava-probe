package org.openjava.probe.client.gui;

import org.openjava.probe.client.console.JavaProcess;
import org.openjava.probe.client.console.ShellConsole;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProcessDialog extends JDialog {
    private final ShellConsole console;
    private final DefaultListModel<JavaProcess> listModel = new DefaultListModel<>();

    public ProcessDialog(ShellConsole console, JFrame owner) {
        super(owner);
        this.console = console;
        setTitle("Select a java process...");
        setSize(460, 300);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        initLayout();
    }

    public void showDialog() {
        listModel.clear();
        List<JavaProcess> processes = console.listJavaProcesses();
        listModel.addAll(processes);
        setVisible(true);
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JPanel processPanel = new JPanel(new GridLayout(1, 1));
        processPanel.setPreferredSize(new Dimension(300, 140));
        JList processList = new JList(listModel);
        processList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Object o = processList.getSelectedValue();
                    if (o != null && o instanceof JavaProcess) {
                        setVisible(false);
                        GuiEventMulticaster.getInstance().attachJavaProcess((JavaProcess) o);
                    }
                }
            }
        });
        processPanel.add(processList);

        add(processPanel, BorderLayout.CENTER);
        JPanel selectPanel = new JPanel(new FlowLayout());
        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(event -> {
            Object o = processList.getSelectedValue();
            if (o != null && o instanceof JavaProcess) {
                setVisible(false);
                GuiEventMulticaster.getInstance().attachJavaProcess((JavaProcess) o);
            }
        });
        selectPanel.add(connectBtn);
        add(selectPanel, BorderLayout.SOUTH);
    }
}
