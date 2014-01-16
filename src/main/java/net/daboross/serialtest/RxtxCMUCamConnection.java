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
import java.io.IOException;

public class RxtxCMUCamConnection extends CMUCamConnection {

    private final SerialPort port;

    public RxtxCMUCamConnection() throws IOException {
        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0");
        } catch (NoSuchPortException e) {
            SkyLog.log("Couldn't find port /dev/USB0");
            throw new IOException(e);
        }
        if (portIdentifier.isCurrentlyOwned()) {
            SkyLog.err("Port " + portIdentifier.getName() + " is already owned.");
            throw new IOException("PortInUse");
        }
        CommPort commPort;
        try {
            commPort = portIdentifier.open("CMUCamJava", 2000);
        } catch (PortInUseException e) {
            throw new IOException("Unexpected PortInUseException", e);
        }
        if (!(commPort instanceof SerialPort)) {
            SkyLog.err("Gah, this isn't a serial port.");
            throw new ClassCastException();
        }
        port = (SerialPort) commPort;
        rawInput = port.getInputStream();
        rawOutput = port.getOutputStream();
    }

    public void setBaud(int baud) throws IOException {
        try {
            port.setBaudBase(baud);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException("Unexpected UnsupportedCommOperationException", e);
        }
    }
}
