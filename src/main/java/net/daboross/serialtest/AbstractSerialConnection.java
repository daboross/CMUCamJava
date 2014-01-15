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

import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class AbstractSerialConnection {

    protected InputStream rawInput;
    protected BufferedReader input;
    protected BufferedWriter output;

    protected void init(final InputStream rawInput, final BufferedReader input, final BufferedWriter output) {
        this.rawInput = rawInput;
        this.input = input;
        this.output = output;
    }

    public abstract void begin(int baud) throws IOException, UnsupportedCommOperationException, InterruptedException;

    public abstract void end() throws IOException, InterruptedException;

    public void write(String str) throws IOException {
        output.write(str);
    }

    public void waitForString(String str) throws IOException {
        byte[] bytesToMatch = str.getBytes(Charset.forName("ASCII"));
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

    public String readUntil(String str) throws IOException {
        byte[] bytesToMatch = str.getBytes(Charset.forName("ASCII"));
        int bytesMatched = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (true) {
            if (bytesMatched >= bytesToMatch.length) {
                byte[] allBytesRead = outputStream.toByteArray();
                byte[] bytesNotMatched = Arrays.copyOf(allBytesRead, allBytesRead.length - bytesMatched);
                return new String(bytesNotMatched, Charset.forName("ASCII"));
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

    public InputStream getRawInput() {
        return rawInput;
    }

    public BufferedReader getInput() {
        return input;
    }

    public BufferedWriter getOutput() {
        return output;
    }
}
