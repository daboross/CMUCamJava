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

import java.io.IOException;

public class SerialTestMain implements Runnable {

    private CMUCamConnection c;
    private ColorTrack colorTrack;

    public void run() {
        DebugWindow debug = null;
        try {
            c = new RxtxCMUCamConnection();
            colorTrack = new ColorTrack();
            debug = c.debug;
            debug.addComponent(colorTrack.getCanvas());
            debug.log("[main] Starting");
            c.start();
            debug.log("[main] Started");
            c.sendCommand("CT 1"); // set Color Tracking mode to YUV
            c.sendCommand("AG 0"); // turn off Auto Gain control
            c.sendCommand("AW 0"); // turn off Auto White balance
            c.sendCommand("ST 150 167 18 29 104 118");
            debug.log("[main] Done setting settings");
            c.sendCommand("TC");
            TrackingRead read = new TrackingRead();
            new Thread(read).start();
            for (int i = 0; i < 500; i++) {
                debug.log("[main] Text: '%s'", read.lastResponse);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    debug.log("Unexpected InterruptedException", e);
                }
            }
            read.running = false;
        } catch (IOException ex) {
            System.err.println("Unexpected IOException running");
            if (debug != null) {
                ex.printStackTrace(debug.loggingStream());
            } else {
                ex.printStackTrace();
            }
        } catch (RuntimeException ex) {
            if (debug != null) {
                ex.printStackTrace(debug.loggingStream());
            } else {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SerialTestMain main = new SerialTestMain();
        new Thread(main).start();
    }

    private class TrackingRead implements Runnable {

        private boolean running = true;
        private String lastResponse = "";

        public void run() {
            try {
                while (running) {
                    lastResponse = c.readUntil("\r");
                    try {
                        colorTrack.update(lastResponse);
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace(c.debug.loggingStream());
                    }
                }
                c.write("\r");
                c.readUntilReady();
            } catch (IOException e) {
                c.debug.log("Unexpected IOExceptoin in TrackingRead", e);
            }
        }
    }
}
