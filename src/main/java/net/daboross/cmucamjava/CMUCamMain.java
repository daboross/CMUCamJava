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

public class CMUCamMain {

    public static void main(String[] args) throws IOException {
        // TODO: Is there a better way to store a variable like this rather than a [1] array?
        final CMUColorTracking[] trackingStore = new CMUColorTracking[1];
        CMUCamConnection c = new RxtxCMUCamConnection(new Runnable() {
            public void run() {
                if (trackingStore[0] != null) {
                    try {
                        trackingStore[0].stopTracking();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        c.debug.log("[main] Starting");
        CMUColorTracking tracking = new CMUColorTracking(c, 10);
        trackingStore[0] = tracking;
        ColorTrackingPanel panel = new ColorTrackingPanel();
        c.debug.addComponent(panel);
        tracking.addColorTrackingUpdatable(panel);
        tracking.startTracking();
    }
}
