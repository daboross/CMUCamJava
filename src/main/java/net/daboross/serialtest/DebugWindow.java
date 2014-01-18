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
package net.daboross.serialtest;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class DebugWindow {

    private final JFrame frame = new JFrame();
    private final JTextArea rawText = new JTextArea(30, 40);
    private final JScrollPane rawScroll = new JScrollPane(rawText);
    private final JTextArea loggingText = new JTextArea(30, 40);
    private final JScrollPane loggingScroll = new JScrollPane(loggingText);

    public DebugWindow(final Runnable onEnd) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        JPanel panel = new JPanel();
        final Thread mainThread = Thread.currentThread();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                end(mainThread, onEnd);
            }
        });
        GridLayout layout = new GridLayout();
        panel.setLayout(layout);
        DefaultCaret loggingCaret = (DefaultCaret) loggingText.getCaret();
        loggingCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret rawCaret = (DefaultCaret) rawText.getCaret();
        rawCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(label("Logging Text", loggingScroll));
        panel.add(label("Raw Text", rawScroll));
        frame.add(panel);
        frame.setTitle("SerialTest Debug");
        frame.setVisible(true);
    }

    private JPanel label(String labelString, JComponent component) {
        FlowLayout layout = new FlowLayout();
        JPanel panel = new JPanel(layout);
        panel.add(new JLabel(labelString));
        panel.add(component);
        return panel;
    }

    public void addText(String str) {
        rawText.append(str.replace("\n", "\\n\n").replace("\r", "\\r\n"));
    }

    public void addText(byte b) {
        addText(CSUtils.toString(b));
    }

    public void logText(String str) {
        loggingText.append(str.replace("\r", "\\r").replace("\n", "\\n") + "\n");
    }

    public void logText(byte b) {
        loggingText.append(CSUtils.toString(b).replace("\n", "\\n\n").replace("\r", "\\r\n"));
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
                logText((byte) b);
                System.out.write(b);
            }
        });
    }

    public void log(String msg, Object... args) {
        String message = String.format(msg, args);
        logText(message);
        System.out.println(message);
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            ((Throwable) args[args.length - 1]).printStackTrace(loggingStream());
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
            addText((byte) b);
            return b;
        }
    }

    private class DebugOutputStream extends OutputStream {

        private final OutputStream stream;

        private DebugOutputStream(final OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public void write(final int b) throws IOException {
            addText((byte) b);
            stream.write(b);
        }
    }

    public void end(final Thread mainThread, final Runnable onEnd) {
        new Thread(new Runnable() {
            public void run() {
                log("Ending");
                mainThread.interrupt();
                onEnd.run();
                log("Exiting");
                System.exit(0);
            }
        }).start();
        frame.setVisible(false);
        frame.dispose();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log("Unexpected InterruptedException", ex);
        }
        System.exit(0);
    }
}
