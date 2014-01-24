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
import net.daboross.cmucamjava.api.CMUColorTrackingListener;
import net.daboross.cmucamjava.api.CMUCommandSet;

public class CMUColorTracking extends CMUCommandSet {

    private final Set<CMUColorTrackingListener> listeners = new LinkedHashSet<CMUColorTrackingListener>();
    private final int[] averages = new int[8];
    @SuppressWarnings("unchecked")
    private final Queue<Integer>[] storedValues = new Queue[averages.length];
    private final int pastValuesToAverage;
    private boolean currentlyTracking;

    public CMUColorTracking(final int pastValuesToAverage) {
        this.pastValuesToAverage = pastValuesToAverage;
        for (int i = 0; i < storedValues.length; i++) {
            storedValues[i] = new LinkedList<Integer>();
        }
    }

    @Override
    public void init(CMUCamConnection c) throws IOException {
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
    }

    @Override
    public boolean runWith(final CMUCamConnection c) throws IOException {
        String response = c.readUntil("\r");
        try {
            update(response);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace(c.debug.loggingStream());
        }
        return currentlyTracking;
    }

    @Override
    public void end(final CMUCamConnection c) throws IOException {
        c.write("\r");
    }

    public void stopTrackingNext() throws IOException {
        currentlyTracking = false;
    }

    private void update(String data) {
        if (data.startsWith("T")) {
            updateDataT(data);
        } else {
            throw new IllegalArgumentException(String.format("Unknown data packet format '%s'.", data));
        }
        for (CMUColorTrackingListener listener : listeners) {
            listener.onNewColorTrackingData(averages);
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
                throw new IllegalArgumentException(String.format("Invalid T packet '%s': value '%s' is not an integer.", packet, split[i + 1]), ex);
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

    public void registerListener(CMUColorTrackingListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(CMUColorTrackingListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
}
