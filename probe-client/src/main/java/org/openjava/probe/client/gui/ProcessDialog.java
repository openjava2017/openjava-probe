package org.openjava.probe.client.gui;

import org.openjava.probe.client.console.JavaProcess;
import org.openjava.probe.client.console.ShellConsole;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProcessDialog extends JDialog {
    private final ShellConsole console;
    private final DefaultListModel<JavaProcess> listModel = new DefaultListModel<>();

    public ProcessDialog(ShellConsole console, JFrame owner) {
        super(owner);
        this.console = console;
        setTitle("Select a java process...");
        setSize(340, 200);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        initLayout();
    }

    public void showDialog() {
        listModel.clear();
        SwingUtilities.invokeLater(() -> {
            List<JavaProcess> processes = console.listJavaProcesses();
            listModel.addAll(processes);

        });
        setVisible(true);
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JPanel processPanel = new JPanel(new GridLayout(1, 1));
        processPanel.setPreferredSize(new Dimension(300, 140));
        JList processList = new JList(listModel);
        processPanel.add(processList);
        add(processPanel, BorderLayout.CENTER);
        JPanel selectPanel = new JPanel(new FlowLayout());
        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(event -> {
            Object o = processList.getSelectedValue();
            if (o != null && o instanceof JavaProcess) {
                setVisible(false);
                ((ProbeDashboard)getParent()).attach((JavaProcess) o);
            }
        });
        selectPanel.add(connectBtn);
        add(selectPanel, BorderLayout.SOUTH);
    }
}
