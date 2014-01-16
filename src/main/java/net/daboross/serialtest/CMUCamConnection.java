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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public abstract class CMUCamConnection {

    private final Charset charset = Charset.forName("ASCII");
    protected InputStream rawInput;
    protected BufferedReader input;
    protected OutputStream rawOutput;
    protected BufferedWriter output;
    private State state;

    public void start() throws IOException, InterruptedException {
        if (state != State.NOT_STARTED) {
            return;
        }
        input = new BufferedReader(new InputStreamReader(rawInput, charset));
        output = new BufferedWriter(new OutputStreamWriter(rawOutput, charset));
        state = State.RUNNING_COMMAND;
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        setBaud(19200);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        output.write("\rRS\r");
        waitUntil("CMUcam4 v");
        String version = readUntil("\r");
        SkyLog.log("Connected to CMUcam4 version " + version + ".");
    }

    public void end() throws IOException, InterruptedException {
        if (state == State.NOT_STARTED) {
            return;
        }
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        output.flush();
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        state = State.NOT_STARTED;
    }

    public String readUntilReady() throws IOException {
        if (state == State.READY_FOR_COMMAND) {
            return "";
        } else if (state == State.RUNNING_COMMAND) {
            String result = readUntil("\r:");
            state = State.READY_FOR_COMMAND;
            return result;
        } else {
            throw new NotStartedException();
        }
    }

    public void waitTillReadyForCommand() throws IOException {
        if (state == State.READY_FOR_COMMAND) {
            return;
        } else if (state == State.RUNNING_COMMAND) {
            waitUntil("\r:");
            state = State.READY_FOR_COMMAND;
        } else {
            throw new NotStartedException();
        }
    }

    public boolean sendCommand(String command) throws IOException {
        waitTillReadyForCommand();
        state = State.RUNNING_COMMAND;
        output.write(command + "\r");
        String validCommand = readUntil("\r");
        if (validCommand.equals("ACK")) {
            return true;
        } else if (validCommand.equals("NCK")) {
            return false;
        } else {
            SkyLog.log("Invalid command response, not ACK or NCK: '" + validCommand + "'. Assuming NCK.");
            return false;
        }
    }

    public void waitUntil(String str) throws IOException {
        waitUntil(str.getBytes(charset));
    }

    public String readUntil(String str) throws IOException {
        return new String(readUntil(str.getBytes(charset)), charset);
    }

    public void waitUntil(byte[] bytesToMatch) throws IOException {
        if (state == State.NOT_STARTED) {
            throw new NotStartedException();
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
            throw new NotStartedException();
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
            throw new NotStartedException();
        }
        output.write(str);
    }

    protected abstract void setBaud(int baud) throws IOException;

    private static enum State {
        READY_FOR_COMMAND, RUNNING_COMMAND, NOT_STARTED;
    }
}
