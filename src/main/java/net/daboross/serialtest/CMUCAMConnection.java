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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class CMUCAMConnection {

    private final SerialPort port;
    private final InputStream inputR;
    private final BufferedReader input;
    private final BufferedWriter output;

    public CMUCAMConnection() throws PortInUseException, IOException, NoSuchPortException {
        CommPortIdentifier portIdentifier = null;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0");
        } catch (NoSuchPortException e) {
            SkyLog.log("Couldn't find port /dev/USB0");
            throw e;
        }
        if (portIdentifier.isCurrentlyOwned()) {
            SkyLog.err("Port " + portIdentifier.getName() + " is already owned.");
            throw new PortInUseException();
        }
        CommPort commPort = portIdentifier.open("CMUCamJava", 2000);
        if (!(commPort instanceof SerialPort)) {
            SkyLog.err("Gah, this isn't a serial port.");
            throw new ClassCastException();
        }
        port = (SerialPort) commPort;
        inputR = port.getInputStream();
        input = new BufferedReader(new InputStreamReader(inputR, Charset.forName("ASCII")));
        output = new BufferedWriter(new OutputStreamWriter(port.getOutputStream(), Charset.forName("ASCII")));
    }

    public void start() throws InterruptedException, UnsupportedCommOperationException, IOException {
//        begin(230400);
//        begin(115200);
        begin(19200);
        SkyLog.log("begin()");
        write("\0\0\0\rRS\r");
        SkyLog.log("wrote");
//        output.flush();
//        SkyLog.log("flush");
        String line;
        do {
            SkyLog.log("Reading line");
            line = input.readLine();
        } while (line.isEmpty());
        SkyLog.log("Line is '%s'", line);
    }

    public void begin(int baud) throws IOException, UnsupportedCommOperationException, InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        port.setBaudBase(baud);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }

    public void end() throws IOException, InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        output.flush();
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }

    public void write(String str) throws IOException {
        output.write(str);
    }
}
