/*
 * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.cmucamjava;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import org.jdesktop.swingx.JXCollapsiblePane;

public class CMUCamJavaWindow implements AbstractDebug {

    private final GridBagConstraints constraints = new GridBagConstraints();
    private final JTextArea loggingText = new JTextArea(30, 40);
    private final JTextArea rawText = new JTextArea(30, 40);
    private final JFrame frame = new JFrame();
    private final JPanel panel = new JPanel();

    public CMUCamJavaWindow(final Runnable onEnd) {
        constraints.fill = GridBagConstraints.VERTICAL;
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                end(onEnd);
            }
        });
        panel.setLayout(new GridBagLayout());
        DefaultCaret loggingCaret = (DefaultCaret) loggingText.getCaret();
        loggingCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret rawCaret = (DefaultCaret) rawText.getCaret();
        rawCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        addComponent(prepareLogging("Logging Text", loggingText));
        addComponent(prepareLogging("Raw Text", rawText));
        frame.add(panel);
        frame.setTitle("CMUCam Java");
        frame.setVisible(true);
    }

    private JPanel prepareLogging(final String label, JComponent component) {
        final JButton collapse = new JButton("Hide");
        final JXCollapsiblePane collapsiblePane = new JXCollapsiblePane();
        collapsiblePane.add(new JScrollPane(component));
        collapse.addActionListener(new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (collapsiblePane.isCollapsed()) {
                    collapsiblePane.setCollapsed(false);
                    collapse.setText("Hide");
                } else {
                    collapsiblePane.setCollapsed(true);
                    collapse.setText(label);
                }
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE;
        constraints.gridy = 1;
        panel.add(new JLabel(label), constraints);
        constraints.anchor = GridBagConstraints.BELOW_BASELINE;
        constraints.gridy = 2;
        panel.add(collapsiblePane, constraints);
        constraints.anchor = GridBagConstraints.BELOW_BASELINE;
        constraints.gridy = 3;
        panel.add(collapse, constraints);
        return panel;
    }

    public void addComponent(Component component) {
        constraints.gridx++;
        panel.add(component, constraints);
    }

    private void addRawText(byte b) {
        String text = CMUUtils.toString(b);
        if (text.equals("\n")) {
            text = "\\n\n";
        } else if (text.equals("\r")) {
            text = "\\r\n";
        }
        rawText.append(text);
    }

    public InputStream wrapRawStream(InputStream stream) {
        return new DebugInputStream(stream);
    }

    public OutputStream wrapRawStream(OutputStream stream) {
        return new DebugOutputStream(stream);
    }

    public PrintStream loggingStream() {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                loggingText.append(CMUUtils.toString((byte) b));
                System.out.write(b);
            }
        });
    }

    public void log(String msg, Object... args) {
        String message = String.format("[%s] %s\n", new SimpleDateFormat("HH:mm:ss").format(new Date()), String.format(msg, args));
        loggingText.append(message);
        System.out.print(message);
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                ((Throwable) arg).printStackTrace(loggingStream());
            }
        }
    }

    private class DebugInputStream extends InputStream {

        private final InputStream stream;

        private DebugInputStream(final InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            int b = stream.read();
            addRawText((byte) b);
            return b;
        }

        @Override
        public void close() throws IOException {
            stream.close();
            super.close();
        }
    }

    private class DebugOutputStream extends OutputStream {

        private final OutputStream stream;

        private DebugOutputStream(final OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public void write(final int b) throws IOException {
            addRawText((byte) b);
            stream.write(b);
        }

        @Override
        public void close() throws IOException {
            stream.close();
            super.close();
        }
    }

    public void end(final Runnable onEnd) {
        final Object lock = new Object();
        new Thread(new Runnable() {
            public void run() {
                log("[final] Ending");
                onEnd.run();
                log("[final] Exited cleanly");
                synchronized (lock) {
                    lock.notify();
                }
            }
        }).start();
        frame.setVisible(false);
        frame.dispose();
        try {
            synchronized (lock) {
                lock.wait(500);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}
