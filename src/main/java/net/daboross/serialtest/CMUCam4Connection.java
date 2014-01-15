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
import java.io.IOException;

public class CMUCam4Connection {

    private final AbstractSerialConnection c;
    private boolean readyForCommand;

    public CMUCam4Connection(final AbstractSerialConnection connection) {
        this.c = connection;
    }

    public synchronized void start() throws InterruptedException, UnsupportedCommOperationException, IOException {
        c.begin(19200);
        c.write("\rRS\r");
        c.waitForString("CMUcam4 v");
        String version = c.readUntil("\r");
        SkyLog.log("Connected to CMUcam4 version " + version + ".");
    }

    public void sendSettings() throws IOException {
        sendCommand("CT 1"); // set Color Tracking mode to YUV
//        sendCommand("LM 1"); // set Line tracking Mode on
        sendCommand("AG 0"); // turn off Auto Gain control
        sendCommand("AW 0"); // turn off Auto White balance
        sendCommand("ST 118 133 165 176 124 143");
    }

    public synchronized String readUntilCommandDone() throws IOException {
        if (readyForCommand) {
            return "";
        } else {
            readyForCommand = true;
            return c.readUntil("\r:");
        }
    }

    public synchronized void waitTillReadyForCommand() throws IOException {
        if (readyForCommand) {
            return;
        } else {
            c.waitForString("\r:");
            readyForCommand = true;
        }
    }

    public synchronized boolean sendCommand(String command) throws IOException {
        waitTillReadyForCommand();
        readyForCommand = false;
        c.write(command + "\r");
        String validCommand = c.readUntil("\r");
        if (validCommand.equals("ACK")) {
            return true;
        } else if (validCommand.equals("NCK")) {
            return false;
        } else {
            SkyLog.log("Invalid command response, not ACK or NCK: '" + validCommand + "'. Assuming NCK.");
            return false;
        }
    }
}
