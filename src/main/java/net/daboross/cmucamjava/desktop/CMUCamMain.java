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
package net.daboross.cmucamjava.desktop;

import java.io.IOException;
import net.daboross.cmucamjava.CMUCamConnection;
import net.daboross.cmucamjava.CMUColorTracking;

public class CMUCamMain {

    private CMUColorTracking tracking;
    private CMUCamConnection cmu;

    public static void main(String[] args) throws IOException {
        new CMUCamMain().start();
    }

    public void start() throws IOException {
        Runnable end = new EndRunnable();
        tracking = new CMUColorTracking(10);
        ColorTrackingPanel panel = new ColorTrackingPanel();
        CMUCamWindow debug = new CMUCamWindow(end);
        tracking.registerListener(panel);
        debug.addComponent(panel);
        cmu = new RxtxCMUCamConnection(debug);
        debug.log("[main] Starting");
        cmu.start();
        cmu.runCommandSet(tracking);
        debug.end(end);
    }

    private class EndRunnable implements Runnable {

        public void run() {
            try {
                tracking.stopTrackingNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (cmu != null) {
                try {
                    cmu.end();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
