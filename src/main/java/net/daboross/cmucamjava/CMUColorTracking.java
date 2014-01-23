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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CMUColorTracking {

    private final Set<ColorTrackingUpdatable> updatables = new LinkedHashSet<ColorTrackingUpdatable>();
    private final int[] averages = new int[8];
    @SuppressWarnings("unchecked")
    private final Queue<Integer>[] storedValues = new Queue[averages.length];
    private final CMUCamConnection c;
    private final int pastValuesToAverage;
    private boolean currentlyTracking;
    private CMUCamTrackingReadThread thread;

    public CMUColorTracking(final CMUCamConnection connection, final int pastValuesToAverage) {
        this.c = connection;
        this.pastValuesToAverage = pastValuesToAverage;
        for (int i = 0; i < storedValues.length; i++) {
            storedValues[i] = new LinkedList<Integer>();
        }
    }

    public void startTracking() throws IOException {
        if (currentlyTracking) {
            return;
        }
        currentlyTracking = true;
        c.start();
        c.debug.log("[tracking] Adjusting settings");
        c.sendCommand("CT 1"); // set Color Tracking mode to YUV
        c.sendCommand("AG 0"); // turn off Auto Gain control
        c.sendCommand("AW 0"); // turn off Auto White balance
        c.sendCommand("ST 150 167 18 29 104 118");
        c.debug.log("[tracking] Starting tracking");
        c.sendCommand("TC");
        thread = new CMUCamTrackingReadThread();
        thread.start();
    }

    public void stopTracking() throws IOException {
        currentlyTracking = false;
        if (thread != null) {
            thread.interrupt();
        }
        c.write("\r");
    }

    private void update(String data) {
        if (data.startsWith("T")) {
            updateDataT(data);
        } else {
            throw new IllegalArgumentException(String.format("Unknown data packet format '%s'.", data));
        }
        for (ColorTrackingUpdatable updatable : updatables) {
            updatable.update(averages);
        }
    }

    private void updateDataT(String packet) {
        String[] split = packet.split(" ");
        int[] newValues = new int[averages.length];
        if (split.length < newValues.length + 1) {
            throw new IllegalArgumentException(String.format("Invalid T packet '%s': incomplete number of values", packet));
        }
        for (int i = 0; i < newValues.length; i++) {
            try {
                newValues[i] = Integer.parseInt(split[i + 1]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid T packet '" + packet + "': value '" + split[i + 1] + "' is not an integer.", ex);
            }
        }
        updateAverages(newValues);
    }

    private void updateAverages(int[] newValues) {
        for (int i = 0; i < storedValues.length; i++) {
            Queue<Integer> queue = storedValues[i];
            queue.add(newValues[i]);
            if (queue.size() > pastValuesToAverage) {
                queue.poll();
            }
            averages[i] = CMUUtils.average(queue);
        }
    }

    public void addColorTrackingUpdatable(ColorTrackingUpdatable updatable) {
        if (updatable != null) {
            updatables.add(updatable);
        }
    }

    public int[] getCurrentAverages() {
        return averages;
    }

    public interface ColorTrackingUpdatable {

        /**
         * @param arguments int[8]  containing color tracking information
         *                  (mx my x1 y1 x2 y2 pixels confidence)
         */
        public void update(int[] arguments);
    }

    private class CMUCamTrackingReadThread extends Thread {

        public CMUCamTrackingReadThread() {
            super("CMUCam Tracking Read Thread");
        }

        public void run() {
            try {
                while (currentlyTracking) {
                    String response = c.readUntil("\r");
                    try {
                        update(response);
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace(c.debug.loggingStream());
                    }
                }
            } catch (IOException e) {
                c.debug.log("Unexpected IOException in CMUCamTrackingReadThread", e);
            }
        }
    }
}
