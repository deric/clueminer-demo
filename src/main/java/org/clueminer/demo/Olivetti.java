/*
 * Copyright (C) 2011-2015 clueminer.org
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.clueminer.attributes.BasicAttrType;
import org.clueminer.dataset.plugin.ArrayDataset;
import org.clueminer.dataset.row.IntegerDataRow;
import org.clueminer.demo.gui.faces.FacePanel;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 */
public class Olivetti extends BaseFrame {

    private static final long serialVersionUID = -7539640234381467820L;
    private static final Logger logger = Logger.getLogger(Olivetti.class.getName());
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

        ResLoader loader = new ResLoader();

        final int cntImages = 400;

        File f = loader.resource("faces.csv");
        //final int[][] images = new int[cntImages][4096];
        int attrCnt = 4096;
        ArrayDataset<IntegerDataRow> dataset = new ArrayDataset<>(cntImages, attrCnt);
        for (int j = 0; j < attrCnt; j++) {
            dataset.attributeBuilder().create("attr_" + j, BasicAttrType.NUMERIC);
        }

        logger.info("loading data...");
        long start = System.currentTimeMillis();
        int i = 0;
        IntegerDataRow inst;
        int cls = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] bytes = line.split(",");
                inst = new IntegerDataRow(dataset.attributeCount());
                dataset.add(inst);
                for (int j = 0; j < bytes.length; j++) {
                    inst.set(j, Integer.valueOf(bytes[j]));
                }
                inst.setClassValue(cls);
                if (i % 10 == 9) {
                    cls++;
                }
                i++;
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        logger.log(Level.INFO, "data loaded in {0}ms", (System.currentTimeMillis() - start));

        panel = new FacePanel(dataset);

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
