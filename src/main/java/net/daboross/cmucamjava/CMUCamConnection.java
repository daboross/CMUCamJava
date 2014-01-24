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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import net.daboross.cmucamjava.api.CMUCommandSet;

public abstract class CMUCamConnection {

    private final Object lock = new Object();
    private InputStream rawInput;
    private OutputStream rawOutput;
    private Writer output;
    private State state = State.NOT_STARTED;
    public final AbstractDebug debug;

    public CMUCamConnection(final AbstractDebug debug) {
        this.debug = debug;
    }

    protected void init(InputStream rawInput, OutputStream rawOutput) {
        this.rawInput = debug.wrapRawStream(rawInput);
        this.rawOutput = debug.wrapRawStream(rawOutput);
    }

    public void start() throws IOException {
        synchronized (lock) {
            output = new OutputStreamWriter(rawOutput, CMUUtils.CHARSET);
            state = State.RUNNING_COMMAND;
            setBaud(19200);
            write("\rRS\r");
            debug.log("[cmu] Resetting system");
            waitUntil("CMUcam4 v");
            String version = readUntil("\r");
            debug.log("[cmu] Connected to CMUcam4 version '%s'.", version);
        }
    }

    public void end() throws IOException {
        synchronized (lock) {
            if (state == State.NOT_STARTED) {
                return;
            }
            state = State.NOT_STARTED;
            rawInput.close();
            output.close();
            rawOutput.close();
            close();
            debug.log("[cmu] Closed");
        }
    }

    public void runCommandSet(CMUCommandSet set) throws IOException {
        synchronized (lock) {
            debug.log("[cmu] Running command set");
            set.init(this);
            while (state != State.NOT_STARTED) {
                if (!set.runWith(this)) {
                    break;
                }
            }
            set.end(this);
        }
    }

    public boolean sendCommand(String command) throws IOException {
        synchronized (lock) {
            if (state == State.NOT_STARTED) {
                throw new IllegalStateException("Not started");
            } else if (state == State.RUNNING_COMMAND) {
                waitUntil("\r:");
            } else if (state == State.NEWLINE_READ) {
                waitUntil(":");
            }
            state = State.RUNNING_COMMAND;
            write(command + "\r");
            String validCommand = readUntil("\r");
            if (validCommand.equals("ACK")) {
                return true;
            } else if (validCommand.equals("NCK")) {
                return false;
            } else {
                debug.log("[sendCommand] Invalid command response, not ACK or NCK: '%s'. Assuming NCK.", validCommand);
                return false;
            }
        }
    }

    public void waitUntil(String str) throws IOException {
        synchronized (lock) {
            waitUntil(CMUUtils.toBytes(str));
            if (str.endsWith("\r")) {
                state = State.NEWLINE_READ;
            } else if (str.endsWith("\r:")) {
                state = State.READY_FOR_COMMAND;
            }
        }
    }

    public String readUntil(String str) throws IOException {
        synchronized (lock) {
            String result = CMUUtils.toString(readUntil(CMUUtils.toBytes(str)));
            if (str.endsWith("\r")) {
                state = State.NEWLINE_READ;
            } else if (str.endsWith("\r:")) {
                state = State.READY_FOR_COMMAND;
            }
            return result;
        }
    }

    public void waitUntil(byte[] bytesToMatch) throws IOException {
        synchronized (lock) {
            if (state == State.NOT_STARTED) {
                throw new IllegalStateException("Not started");
            }
            int bytesMatched = 0;
            while (true) {
                if (bytesMatched >= bytesToMatch.length) {
                    return;
                }
                int b = rawInput.read();
                if (b == bytesToMatch[bytesMatched]) {
                    bytesMatched++;
                } else {
                    bytesMatched = 0;
                }
            }
        }
    }

    public byte[] readUntil(byte[] bytesToMatch) throws IOException {
        synchronized (lock) {
            if (state == State.NOT_STARTED) {
                throw new IllegalStateException("Not started");
            }
            int bytesMatched = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (true) {
                if (bytesMatched >= bytesToMatch.length) {
                    byte[] allBytesRead = outputStream.toByteArray();
                    return Arrays.copyOf(allBytesRead, allBytesRead.length - bytesMatched);
                }
                int b = rawInput.read();
                if (b == bytesToMatch[bytesMatched]) {
                    bytesMatched++;
                } else {
                    bytesMatched = 0;
                }
                outputStream.write(b);
            }
        }
    }

    public void write(String str) throws IOException {
        synchronized (lock) {
            if (state == State.NOT_STARTED) {
                throw new IllegalStateException("Not started");
            }
            output.write(str);
            output.flush();
            rawOutput.flush();
        }
    }

    protected abstract void setBaud(int baud) throws IOException;

    protected abstract void close() throws IOException;

    private static enum State {
        READY_FOR_COMMAND, RUNNING_COMMAND, NEWLINE_READ, NOT_STARTED
    }
}
