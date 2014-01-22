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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public abstract class CMUCamConnection {

    private InputStream rawInput;
    private OutputStream rawOutput;
    private Writer output;
    private State state = State.NOT_STARTED;
    public final CMUCamJavaWindow debug;

    protected CMUCamConnection(final Runnable endingRunnable) {
        debug = new CMUCamJavaWindow(new Runnable() {
            public void run() {
                try {
                    if (endingRunnable != null) {
                        endingRunnable.run();
                    }
                    end();
                } catch (IOException e) {
                    debug.log("Unexpected IOException", e);
                }
            }
        });
    }

    protected void init(InputStream rawInput, OutputStream rawOutput) {
        this.rawInput = debug.wrapRawStream(rawInput);
        this.rawOutput = debug.wrapRawStream(rawOutput);
    }

    public void start() throws IOException {
        output = new OutputStreamWriter(rawOutput, CMUUtils.CHARSET);
        state = State.RUNNING_COMMAND;
        setBaud(19200);
        write("\rRS\r");
        debug.log("[reset] Resetting system");
        waitUntil("CMUcam4 v");
        String version = readUntil("\r");
        debug.log("[reset] Connected to CMUcam4 version '%s'.", version);
    }

    public void end() throws IOException {
        if (state == State.NOT_STARTED) {
            return;
        }
        rawInput.close();
        output.close();
        rawOutput.close();
        debug.log("Closing");
        close();
        state = State.NOT_STARTED;
    }

    public String readUntilReady() throws IOException {
        if (state == State.READY_FOR_COMMAND) {
            return "";
        } else if (state == State.RUNNING_COMMAND) {
            String result = readUntil("\r:");
            state = State.READY_FOR_COMMAND;
            return result;
        } else if (state == State.NEWLINE_READ) {
            String result = readUntil(":");
            state = State.READY_FOR_COMMAND;
            return result;
        } else {
            throw new IllegalStateException("Not started");
        }
    }

    public void waitTillReadyForCommand() throws IOException {
        if (state == State.RUNNING_COMMAND) {
            waitUntil("\r:");
            state = State.READY_FOR_COMMAND;
        } else if (state == State.NEWLINE_READ) {
            waitUntil(":");
        } else if (state != State.READY_FOR_COMMAND) {
            throw new IllegalStateException("Not started");
        }
    }

    public boolean sendCommand(String command) throws IOException {
        waitTillReadyForCommand();
//        debug.log("[sendCommand] Sending '%s'", command);
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

    public void waitUntil(String str) throws IOException {
        waitUntil(CMUUtils.toBytes(str));
        if (str.endsWith("\r")) {
            state = State.NEWLINE_READ;
        }
    }

    public String readUntil(String str) throws IOException {
        String result = CMUUtils.toString(readUntil(CMUUtils.toBytes(str)));
        if (str.endsWith("\r")) {
            state = State.NEWLINE_READ;
        }
        return result;
    }

    public void waitUntil(byte[] bytesToMatch) throws IOException {
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

    public byte[] readUntil(byte[] bytesToMatch) throws IOException {
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

    public void write(String str) throws IOException {
        if (state == State.NOT_STARTED) {
            throw new IllegalStateException("Not started");
        }
        output.write(str);
        output.flush();
        rawOutput.flush();
    }

    protected abstract void setBaud(int baud) throws IOException;

    protected abstract void close() throws IOException;

    private static enum State {
        READY_FOR_COMMAND, RUNNING_COMMAND, NEWLINE_READ, NOT_STARTED
    }
}
