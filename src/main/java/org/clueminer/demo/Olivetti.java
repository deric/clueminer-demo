/*
 * Copyright (C) 2011-2018 clueminer.org
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
package org.clueminer.demo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.clueminer.demo.gui.faces.FacePanel;
import org.clueminer.demo.gui.faces.FacesProvider;

/**
 *
 * @author deric
 */
public class Olivetti extends BaseFrame {

    private static final long serialVersionUID = -7539640234381467820L;
    private static final Logger LOGGER = Logger.getLogger(Olivetti.class.getName());
    private JPanel panel;

    public Olivetti() {
        this.title = "Olivetti";
        initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Olivetti().showInFrame();
            }
        });
    }

    private void initComponents() {
        setSize(800, 800);

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = c.weighty = 1.0; //no fill while resize

        FacesProvider fp = new FacesProvider();
        fp.load();
        panel = new FacePanel(fp.first());
        panel.setSize(this.getSize());
        add(panel, c);

        setVisible(true);
    }

    /**
     *
     * @param width The image width (height derived from buffer length)
     * @param height
     * @param buffer The buffer containing raw grayscale pixel data
     *
     * @return The grayscale image
     */
    public static BufferedImage getGrayscale(int width, int height, byte[] buffer) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        int[] nBits = {8};
        ColorModel cm = new ComponentColorModel(cs, nBits, false, true,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        SampleModel sm = cm.createCompatibleSampleModel(width, height);
        DataBufferByte db = new DataBufferByte(buffer, width * height);
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        BufferedImage result = new BufferedImage(cm, raster, false, null);

        return result;
    }

}
