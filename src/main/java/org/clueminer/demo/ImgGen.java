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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JPanel;
import org.clueminer.data.DataLoader;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.demo.gui.CliPlot;
import org.clueminer.demo.gui.ScatterViewer;

/**
 * Generate simple dataset visualizations to given image format
 *
 * @author deric
 */
public class ImgGen {

    public static void main(String[] args) {

        final ImgParams params = new ImgParams();
        JCommander cmd = new JCommander(params);
        printUsage(args, cmd, params);

        final String cat = "artificial";
        final DataProvider data = DataLoader.createLoader("datasets", cat);
        String[] datasets = data.getDatasetNames();

        final ScatterViewer scatter = new ScatterViewer(data);

        CliPlot plot = new CliPlot();
        plot.setSize(params.width, params.height);
        Dataset d;
        for (String name : datasets) {
            d = data.getDataset(name);
            System.out.println("rendering " + name);
            plot.setClustering(scatter.goldenClustering(d));
            plot.repaint();
            File file = new File(workDir(params, cat) + File.separatorChar + safeName(name) + "." + params.format);
            plot.saveFile(file, params.format);
        }

    }

    public static String safeName(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    public static BufferedImage getBufferedImage(JPanel panel, int w, int h) {
        Dimension dim = panel.getSize();
        int width, height;

        width = Math.max(w, dim.width);
        height = Math.max(h, dim.height);

        panel.setSize(width, height);

        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        panel.validate();
        panel.revalidate();
        panel.paint(g);
        panel.repaint();

        g.dispose();
        return bi;
    }

    public static String workDir(ImgParams params, String subdir) {
        String path = params.home + File.separatorChar + subdir;
        return mkdir(path);
    }

    public static String mkdir(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Failed to create " + folder + " !");
            }
        }
        return file.getAbsolutePath();
    }

    public static void printUsage(String[] args, JCommander cmd, ImgParams params) {
        try {
            cmd.parse(args);

        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
            cmd.usage();
            System.exit(0);
        }
    }

}
