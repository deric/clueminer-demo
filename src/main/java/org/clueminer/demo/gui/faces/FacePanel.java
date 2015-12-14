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
package org.clueminer.demo.gui.faces;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.colors.ColorBrewer;
import org.clueminer.dataset.api.ColorGenerator;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.gui.BPanel;

/**
 * Display dataset consisting of faces
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class FacePanel<E extends Instance, C extends Cluster<E>> extends BPanel {

    private static final long serialVersionUID = 2144561337674137421L;
    private Dataset<E> images;
    private final int width = 64;
    private final int height = 64;
    private int cntImages;
    //private static final Logger LOGGER = Logger.getLogger(FacePanel.class.getName());
    private BufferedImage[] faces;
    private final HashMap<Object, Color> colors = new HashMap<>();
    private final ColorGenerator cg;
    private Clustering<E, C> clustering;

    public FacePanel(Dataset<E> images) {
        this.images = images;
        cntImages = images.size();
        faces = new BufferedImage[cntImages];
        cg = new ColorBrewer();
    }

    public FacePanel() {
        cg = new ColorBrewer();
    }

    public void setClustering(final Clustering<E, C> clustering) {
        this.clustering = clustering;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                cg.reset();
                images = clustering.getLookup().lookup(Dataset.class);
                cntImages = images.size();
                faces = new BufferedImage[cntImages];
                resetCache();
            }
        });
    }

    @Override
    public void render(Graphics2D g) {
        if (!hasData()) {
            return;
        }

        int y = 0;
        int mod;
        BufferedImage img;
        int cls;
        E inst;
        for (int j = 0; j < cntImages; j++) {
            mod = j % 20;
            inst = images.get(j);
            if (faces[j] == null) {
                //cache images (most expensive operation)
                faces[j] = createImg(width, height, inst);
            }
            if (clustering != null) {
                cls = clustering.assignedCluster(inst.getIndex());
            } else {
                cls = (int) inst.classValue();
            }

            if (cls > -1) {
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D gr = img.createGraphics();
                gr.drawImage(deepCopy(faces[j]), 0, 0, null);
                gr.setColor(getColor(cg, cls));
                gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.55f));
                gr.fillRect(0, 0, width, height);
            } else {
                img = faces[j];
            }

            g.drawImage(img, mod * width, y * height, null);
            if (mod == 19) {
                y++;
            }
        }
    }

    private Color getColor(ColorGenerator cg, Object klass) {
        if (!colors.containsKey(klass)) {
            colors.put(klass, cg.next());
        }
        return colors.get(klass);
    }

    private BufferedImage createImg(int width, int height, E data) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_INDEXED);
        img = op.filter(img, null);
        int pos;

        WritableRaster raster = img.getRaster();
        //double val;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pos = (j * width) + i;
                //val = data[pos] + (data[pos] << 8) + (data[pos] << 16);
                raster.setSample(j, i, 0, data.get(pos));
                //raster.setSample(j, i, 0, val);
            }
        }

        return img;
    }

    /**
     * Deep copy of an image
     *
     * @param bi
     * @return
     */
    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    @Override
    public void sizeUpdated(Dimension size) {
        resetCache();
    }

    @Override
    public boolean hasData() {
        return (images != null);
    }

    @Override
    public void recalculate() {
        //TODO: maybe parametrize this
        realSize.width = width * 20;
        realSize.height = height * 20;
    }

    @Override
    public boolean isAntiAliasing() {
        return true;
    }

}
