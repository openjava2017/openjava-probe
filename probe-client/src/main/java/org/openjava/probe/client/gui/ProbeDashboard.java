package org.openjava.probe.client.gui;

import org.openjava.probe.client.agent.ProbeAgentClient;
import org.openjava.probe.client.console.JavaProcess;
import org.openjava.probe.client.console.ShellConsole;
import org.openjava.probe.client.env.Environment;
import org.openjava.probe.shared.exception.ProbeServiceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProbeDashboard extends JFrame {
    private final Environment environment;
    private final ShellConsole console;
    private final ProcessDialog processDialog;

    public ProbeDashboard(Environment environment) {
        this.environment = environment;
        this.console = new ShellConsole(environment);
        this.processDialog = new ProcessDialog(console, this);
        setTitle("Dashboard");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMenuBar();
        initLayout();
        setVisible(true);
    }

    public void attach(JavaProcess process) {
        StatusBar.getInstance().setStatus("");
        try {
            console.attachAgent(process.processId());
        } catch (ProbeServiceException sex) {
            JOptionPane.showMessageDialog(getParent(), sex.getMessage(), "Agent Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getParent(), "System unknown error during agent attaching", "Agent Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ExecutorService executorService = new ThreadPoolExecutor(2, 10, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
        ProbeAgentClient client = new ProbeAgentClient(environment, executorService);
        try {
            client.start();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(getParent(), "Probe agent client start failed", "Agent Error", JOptionPane.ERROR_MESSAGE);
        }
        StatusBar.getInstance().setStatus(process.mainClass() + " connected");
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu connectMenu = new JMenu("Connect");
        connectMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                processDialog.showDialog();
            }
        });
        JMenu disconnectMenu = new JMenu("Disconnect");
        JMenu quitMenu = new JMenu("Quit");
        menuBar.add(connectMenu);
        menuBar.add(disconnectMenu);
        menuBar.add(quitMenu);
        setJMenuBar(menuBar);
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(CommandPanel.getInstance(), BorderLayout.NORTH);
        add(new JScrollPane(ConsolePanel.getInstance()), BorderLayout.CENTER);
        add(StatusBar.getInstance(), BorderLayout.SOUTH);
    }
}
