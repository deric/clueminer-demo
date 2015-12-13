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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import org.clueminer.dataset.plugin.ArrayDataset;
import org.clueminer.dataset.row.IntegerDataRow;
import org.clueminer.gui.BPanel;

/**
 * Display dataset consisting of faces
 *
 * @author deric
 */
public class FacePanel extends BPanel {

    private static final long serialVersionUID = 2144561337674137421L;
    private final ArrayDataset<IntegerDataRow> images;
    private final int width = 64;
    private final int height = 64;
    private final int cntImages;
    //private static final Logger LOGGER = Logger.getLogger(FacePanel.class.getName());
    private BufferedImage[] faces;

    public FacePanel(ArrayDataset<IntegerDataRow> images) {
        this.images = images;
        cntImages = images.size();
        faces = new BufferedImage[cntImages];
    }

    @Override
    public void render(Graphics2D g) {
        int y = 0;
        int mod;
        for (int j = 0; j < cntImages; j++) {
            mod = j % 20;
            if (faces[j] == null) {
                faces[j] = createImg(width, height, images.get(j));
            }
            g.drawImage(faces[j], mod * width, y * height, null);
            if (mod == 19) {
                y++;
            }
        }
    }

    private BufferedImage createImg(int width, int height, IntegerDataRow data) {
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
