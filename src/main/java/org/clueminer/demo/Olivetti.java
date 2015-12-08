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

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 */
public class Olivetti extends BaseFrame {

    private static final long serialVersionUID = -7539640234381467820L;
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
        setSize(800, 600);

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
        final int width = 64;
        final int height = 64;

        File f = loader.resource("faces.csv");
        final int[][] images = new int[cntImages][4096];

        int i = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] bytes = line.split(",");
                for (int j = 0; j < bytes.length; j++) {
                    images[i][j] = Integer.valueOf(bytes[j]);
                }
                i++;
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }


        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int y = 0;
                int mod;
                for (int j = 0; j < cntImages; j++) {
                    mod = j % 10;
                    g.drawImage(createImg(width, height, images[j]), mod * width, y * height, null);
                    if (mod == 9) {
                        y++;
                    }
                }

            }
        };

        add(panel, c);

        setVisible(true);
    }

    private BufferedImage image(String[] bytes) {
        int width = getSize().width;
        int height = getSize().height;
        int[] data = new int[width * height];
        int i = 0;
        for (int y = 0; y < height; y++) {
            int red = (y * 255) / (height - 1);
            for (int x = 0; x < width; x++) {
                int green = (x * 255) / (width - 1);
                int blue = 128;
                data[i++] = (red << 16) | (green << 8) | blue;
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, data, 0, width);

        return image;
    }

    private BufferedImage createImg(int width, int height, int[] data) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_INDEXED);
        img = op.filter(img, null);
        int pos;

        WritableRaster raster = img.getRaster();
        double val;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pos = (j * width) + i;
                //val = data[pos] + (data[pos] << 8) + (data[pos] << 16);
                raster.setSample(j, i, 0, data[pos]);
                //raster.setSample(j, i, 0, val);
            }
        }

        return img;
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
