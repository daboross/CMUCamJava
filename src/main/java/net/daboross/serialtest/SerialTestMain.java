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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;

public class SerialTestMain implements Runnable {

    @Override
    public void run() {
        try {
            USBSerialConnection serialConnection = new USBSerialConnection();
            SkyLog.log("Created");
            CMUCam4Connection cmuCam4Connection = new CMUCam4Connection(serialConnection);
            cmuCam4Connection.start();
            cmuCam4Connection.sendSettings();
        } catch (NoSuchPortException | PortInUseException | IOException | InterruptedException | UnsupportedCommOperationException e) {
            SkyLog.ex(e);
        }
    }

    public static void main(String[] args) {
        SerialTestMain main = new SerialTestMain();
        new Thread(main).start();
    }
}
