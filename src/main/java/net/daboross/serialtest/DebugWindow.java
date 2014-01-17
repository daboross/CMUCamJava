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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import sun.awt.VariableGridLayout;

public class DebugWindow {

    private final JFrame frame = new JFrame();
    private final JTextArea rawText = new JTextArea(10, 60);
    private final JTextArea logging = new JTextArea(10, 60);

    public DebugWindow(final Runnable endIt) {
        JButton button = new JButton();
        JPanel panel = new JPanel();
        button.setText("End");
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                endIt.run();
                frame.setVisible(false);
                frame.dispose();
                System.exit(0);
            }
        });
        panel.setLayout(new FlowLayout());
        logging.setAutoscrolls(true);
        panel.add(combinedElements(new JLabel("Logging", JLabel.CENTER), logging));
        panel.add(combinedElements(new JLabel("Raw Text", JLabel.CENTER), rawText));
        panel.add(button);
        frame.add(panel);
        frame.setTitle("SerialTest Debug");
        frame.setLocation(640, 480);
        frame.setMinimumSize(new Dimension(640, 480));
        frame.setVisible(true);
        frame.pack();
    }

    private JPanel combinedElements(JComponent... components) {
        JPanel panel = new JPanel(new VariableGridLayout(components.length, 1));
        for (JComponent component : components) {
            panel.add(component);
        }
        return panel;
    }

    public void addText(String str) {
        rawText.append(str.replace("\n", "\\n\n").replace("\r", "\\r\n"));
    }

    public void addText(byte b) {
        addText(CSUtils.toString(b));
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
                addText((byte) b);
                System.out.write(b);
            }
        });
    }

    public void log(String msg, Object... args) {
        String message = String.format(msg, args);
        addText(message);
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
}
