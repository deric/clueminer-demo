/*
 * Copyright (C) 2011-2016 clueminer.org
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
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.Executor;
import org.clueminer.colors.ColorBrewer;
import org.clueminer.data.DataLoader;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.impl.ArrayDataset;
import org.clueminer.demo.gui.CliPlot;
import org.clueminer.demo.gui.ScatterViewer;
import org.clueminer.exception.ParserError;
import org.clueminer.io.ARFFHandler;
import org.clueminer.utils.Props;
import org.openide.util.Exceptions;

/**
 * Generate simple dataset visualizations to given image format
 *
 * @author deric
 */
public class ImgGen {

    private static final Logger LOG = Logger.getLogger(ImgGen.class.getName());

    public static void main(String[] args) {
        final ImgParams arg = new ImgParams();
        JCommander cmd = new JCommander(arg);
        printUsage(args, cmd, arg);

        final String cat = "artificial";
        final DataProvider data = DataLoader.createLoader("datasets", cat);
        String[] datasets = data.getDatasetNames();

        final ScatterViewer scatter = new ScatterViewer(data);
        scatter.setSimpleMode(true);

        CliPlot plot = new CliPlot();
        plot.setSimpleMode(true);
        plot.setSize(arg.width, arg.height);
        System.out.println("params: " + arg.getParams());
        System.out.println("exporting data to: " + workDir(arg, cat));
        Dataset d;
        Executor exec = new ClusteringExecutorCached();
        exec.setColorGenerator(new ColorBrewer());
        if (arg.dataset != null) {
            d = getDataset(arg.dataset, arg, data);
            process(d, scatter, plot, arg, cat, exec);
        } else {
            for (String dataset : datasets) {
                d = getDataset(dataset, arg, data);
                process(d, scatter, plot, arg, cat, exec);
            }
        }
    }

    private static Dataset getDataset(String name, ImgParams arg, DataProvider provider) {
        Dataset dataset;
        if (provider.hasDataset(name)) {
            dataset = provider.getDataset(name);
        } else {
            String type = "arff";
            String path = name;
            if (!path.contains(".")) {
                path += "." + type;
            }
            File file = new File(name);
            if (!file.exists()) {
                throw new RuntimeException("could not load " + name);
            }
            dataset = new ArrayDataset(10, 2);
            dataset.setName(name);
            ARFFHandler arff = new ARFFHandler();
            try {
                arff.load(file, dataset);
            } catch (FileNotFoundException | ParserError ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return dataset;
    }

    private static void process(Dataset d, ScatterViewer scatter, CliPlot plot,
            ImgParams arg, final String cat, Executor exec) {
        System.out.println("rendering " + d.getName());
        String output = d.getName();
        if (arg.computeClustering) {
            Props p = new Props();
            if (!arg.getParams().isEmpty()) {
                p = Props.fromJson(arg.getParams());
                if (p.containsKey("algorithm")) {
                    output = p.get("algorithm") + "-" + d.getName();
                }
            }
            Clustering c = exec.clusterRows(d, p);
            plot.setClustering(c);
        } else {
            plot.setClustering(scatter.goldenClustering(d));
        }
        plot.repaint();
        File file = new File(workDir(arg, cat) + File.separatorChar + safeName(output) + "." + arg.format);
        LOG.log(Level.INFO, "writing to {0}", file.getPath());
        plot.saveFile(file, arg.format);
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
