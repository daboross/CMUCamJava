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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.daboross.cmucamjava.api.CMUColorTrackingListener;

public class ColorTrackingPanel extends JPanel implements CMUColorTrackingListener {

    private final ColorTrackingCanvas canvas;
    private final JLabel[] labels;

    public ColorTrackingPanel() {
        super(new GridBagLayout());
        canvas = new ColorTrackingCanvas();
        JPanel textPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        labels = new JLabel[6];
        for (int i = 0; i < 6; i++) {
            labels[i] = new JLabel();
            textPanel.add(labels[i]);
        }
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE;
        constraints.gridy = 1;
        add(canvas, constraints);
        constraints.anchor = GridBagConstraints.BELOW_BASELINE;
        constraints.gridy = 2;
        add(textPanel, constraints);
    }

    public void onNewColorTrackingData(final int[] values) {

        labels[0].setText(String.format("AverageX = %05d", values[0]));
        labels[1].setText(String.format("LeftX = %05d", values[2]));
        labels[2].setText(String.format("TopY = %05d", values[3]));
        labels[3].setText(String.format("AverageY = %05d", values[1]));
        labels[4].setText(String.format("RightX = %05d", values[4]));
        labels[5].setText(String.format("BottomY = %05d", values[5]));
        canvas.setAverageX(values[0] * 2);
        canvas.setAverageY(values[1] * 2);
        canvas.setLeftX(values[2] * 2);
        canvas.setRightX(values[4] * 2);
        canvas.setTopY(values[3] * 2);
        canvas.setBottomY(values[5] * 2);
        canvas.repaint();
    }

    private static class ColorTrackingCanvas extends JPanel {

        private int averageX;
        private int averageY;
        private int leftX;
        private int topY;
        private int rightX;
        private int bottomY;

        public void setAverageX(final int averageX) {
            this.averageX = averageX;
        }

        public void setAverageY(final int averageY) {
            this.averageY = averageY;
        }

        public void setLeftX(final int leftX) {
            this.leftX = leftX;
        }

        public void setTopY(final int topY) {
            this.topY = topY;
        }

        public void setRightX(final int rightX) {
            this.rightX = rightX;
        }

        public void setBottomY(final int bottomY) {
            this.bottomY = bottomY;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(240, 240);
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillRect(0, 0, 240, 240);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(leftX, topY, rightX - leftX, bottomY - topY);
            g.setColor(Color.CYAN);
            int minX = averageX - 3, minY = averageY - 3, xLength = 6, yLength = 6;
            if (minX < 0) minX = 0;
            if (minY < 0) minY = 0;
            if (minX + xLength > 240) xLength = 240 - minX;
            if (minY + yLength > 240) yLength = 240 - minY;
            g.fillRect(minX, minY, xLength, yLength);
        }
    }
}
