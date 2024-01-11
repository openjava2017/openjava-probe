package org.openjava.probe.client.gui;

import org.openjava.probe.client.agent.ProbeAgentClient;
import org.openjava.probe.client.console.JavaProcess;
import org.openjava.probe.client.console.ShellConsole;
import org.openjava.probe.client.context.Environment;
import org.openjava.probe.client.gui.event.AttachEvent;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;
import org.openjava.probe.client.session.Session;
import org.openjava.probe.client.session.SessionState;
import org.openjava.probe.shared.exception.ProbeServiceException;
import org.openjava.probe.shared.message.InfoMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProbeDashboard extends JFrame {
    private final Environment environment;
    private final ShellConsole shellConsole;
    private final JPopupMenu popupMenu;
    private final CommandPanel commandPanel;
    private final ConsolePanel consolePanel;
    private final StatusBar statusBar;
    private final ProcessDialog processDialog;
    private final ProbeAgentClient client;
    private volatile Session session;

    public ProbeDashboard(Environment environment) {
        this.environment = environment;
        this.shellConsole = new ShellConsole(environment);
        this.popupMenu = new JPopupMenu();
        this.commandPanel = new CommandPanel();
        this.consolePanel = new ConsolePanel();
        this.statusBar = new StatusBar();
        this.processDialog = new ProcessDialog(shellConsole, this);
        ExecutorService executorService = new ThreadPoolExecutor(2, 10, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
        this.client = new ProbeAgentClient(environment, executorService);

        setTitle("Dashboard");
        setSize(750, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMenuBar();
        initLayout();
        installEventListener();
    }

    public void showDashboard() throws Exception {
        this.client.start();
        setVisible(true);
    }

    public void attach(JavaProcess process) {
        statusBar.setStatus("");
        try {
            shellConsole.attachAgent(process.processId());
        } catch (ProbeServiceException sex) {
            JOptionPane.showMessageDialog(getParent(), sex.getMessage(), "Agent Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getParent(), "System unknown error during agent attaching", "Agent Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String host = environment.getRequiredProperty("probe.server.host");
            int port = environment.getRequiredProperty("probe.server.port", Integer.class);
            int timeout = environment.getProperty("probe.client.connTimeOut", Integer.class, 4000);
            this.session = client.connect(host, port, timeout);
            GuiEventMulticaster.getInstance().fireAttachEvent(new AttachEvent(process, true));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            GuiEventMulticaster.getInstance().fireAttachEvent(new AttachEvent(process, false));
            JOptionPane.showMessageDialog(getParent(), "Probe agent client start failed", "Agent Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initMenuBar() {
        JMenuItem attachMenuItem = new JMenuItem("Attach...");
        JMenuItem detachMenuItem = new JMenuItem("Detach");
        detachMenuItem.setEnabled(false);
        JMenuItem cancelMenuItem = new JMenuItem("Cancel");
        cancelMenuItem.setEnabled(false);

        popupMenu.add(attachMenuItem);
        popupMenu.add(detachMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(cancelMenuItem);

        consolePanel.addMouseListener(new MouseAdapter() {
            // For mac
            public void mousePressed(MouseEvent event) {
                if(event.isPopupTrigger()) {
                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
            // For windows
            public void mouseReleased(MouseEvent event) {
                if(event.isPopupTrigger()) {
                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });

        attachMenuItem.addActionListener(event -> processDialog.showDialog());
        detachMenuItem.addActionListener(event -> session.send("quit"));
        cancelMenuItem.addActionListener(event -> session.send("cancel"));
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(commandPanel, BorderLayout.NORTH);
        add(new JScrollPane(consolePanel), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void installEventListener() {
        GuiEventMulticaster.getInstance().installProcessConsumer(this::attach);

        GuiEventMulticaster.getInstance().installCommandConsumer(command -> {
            if (session != null) {
                session.send(command);
                consolePanel.info(command);
                commandPanel.clear();
            } else {
                JOptionPane.showMessageDialog(getParent(), "User session not started", "Session Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        GuiEventMulticaster.getInstance().installAttachEventListener(event -> {
            JavaProcess process = (JavaProcess) event.getSource();
            if (event.success()) {
                statusBar.setStatus(process.mainClass() + " connected");
                popupMenu.getComponent(0).setEnabled(false);
                popupMenu.getComponent(1).setEnabled(true);
                popupMenu.getComponent(3).setEnabled(false);
                commandPanel.setEnabled(true);
            } else {
                statusBar.setStatus(process.mainClass() + " connect failed");
                popupMenu.getComponent(0).setEnabled(true);
                popupMenu.getComponent(1).setEnabled(false);
                popupMenu.getComponent(3).setEnabled(false);
                commandPanel.setEnabled(false);
            }
        });

        GuiEventMulticaster.getInstance().installDetachEventListener(event -> {
            session = null;
            SwingUtilities.invokeLater(() -> {
                popupMenu.getComponent(0).setEnabled(true);
                popupMenu.getComponent(1).setEnabled(false);
                popupMenu.getComponent(3).setEnabled(false);
                commandPanel.setEnabled(false);
                consolePanel.clear();
            });
        });

        GuiEventMulticaster.getInstance().installDataEventListener(event -> {
            InfoMessage message = event.message();
            SwingUtilities.invokeLater(() -> {
                switch (message.level()) {
                    case InfoMessage.INFO_LEVEL:
                        consolePanel.info(message.information());
                        break;
                    case InfoMessage.ERROR_LEVEL:
                        consolePanel.error(message.information());
                        break;
                    default:
                }
            });
        });

        GuiEventMulticaster.getInstance().installSessionStateListener(event -> SwingUtilities.invokeLater(() -> {
            if (event.state() == SessionState.IDLE) {
                commandPanel.setEnabled(true);
                popupMenu.getComponent(3).setEnabled(false);
            } else if (event.state() == SessionState.BUSY) {
                commandPanel.setEnabled(false);
                popupMenu.getComponent(3).setEnabled(true);
            }
        }));

        GuiEventMulticaster.getInstance().installDumpEventListener(event -> {
            SwingUtilities.invokeLater(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(String.format("Save %s ...", event.file().name()));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
                    File dir = chooser.getSelectedFile();
                    File dumpFile = new File(dir, event.file().name() + ".class");
                    if (dumpFile.exists()) {
                        dumpFile.delete();
                    }

                    try {
                        if (dumpFile.createNewFile()) {
                            try (FileOutputStream outputStream = new FileOutputStream(dumpFile)) {
                                outputStream.write(event.file().classBytes());
                            }
                            consolePanel.info(String.format("class %s saved successfully", dumpFile.getName()));
                        } else {
                            consolePanel.error("New class file create failed");
                        }
                    } catch (Exception ex) {
                        consolePanel.info(String.format("class %s saved failed", dumpFile.getName()));
                        ex.printStackTrace();
                    }
                }
            });
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    ProbeDashboard.this.client.stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
