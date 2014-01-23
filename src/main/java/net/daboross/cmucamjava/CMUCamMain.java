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

    private CMUColorTracking tracking;
    private ColorTrackingPanel panel;
    private CMUCamJavaWindow debug;
    private CMUCamConnection cmu;

    public static void main(String[] args) throws IOException {
        new CMUCamMain().start();
    }

    public void start() throws IOException {
        debug = new CMUCamJavaWindow(new Runnable() {
            public void run() {
                if (tracking != null) {
                    try {
                        tracking.stopTracking();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (cmu != null) {
                    try {
                        cmu.end();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        cmu = new RxtxCMUCamConnection(debug);
        debug.log("[main] Starting");
        tracking = new CMUColorTracking(cmu, 10);
        panel = new ColorTrackingPanel();
        debug.addComponent(panel);
        tracking.addColorTrackingUpdatable(panel);
        tracking.startTracking();
    }
}
