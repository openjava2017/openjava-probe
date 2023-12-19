package org.openjava.probe.client.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ConsolePanel extends JTextPane {
    private static final ConsolePanel instance = new ConsolePanel();

    private final Document document;
    private final SimpleAttributeSet infoStyle;
    private final SimpleAttributeSet errorStyle;

    private ConsolePanel() {
        document = new DefaultStyledDocument(new StyleContext());
        setDocument(document);

        setPreferredSize(new Dimension(600, 380));
        setEditable(false);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setMargin(new Insets(5, 5, 5, 5));

        infoStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(infoStyle, Color.WHITE);
        StyleConstants.setBackground(infoStyle, Color.BLACK);
        StyleConstants.setFontSize(infoStyle, 14);

        errorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(infoStyle, Color.RED);
        StyleConstants.setBackground(infoStyle, Color.BLACK);
        StyleConstants.setFontSize(infoStyle, 14);
    }

    public static ConsolePanel getInstance() {
        return instance;
    }

    public void info(String line) {
        println(line, infoStyle);
    }

    public void error(String line) {
        println(line, errorStyle);
    }

    public void println(String line, SimpleAttributeSet style) {
        SwingUtilities.invokeLater(() -> {
            try {
                document.insertString(document.getLength(), line + "\n", style);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> {
            try {
                document.remove(0, document.getLength());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
