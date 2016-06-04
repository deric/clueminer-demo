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
package org.clueminer.demo.gui;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.StyleManager;
import com.xeiam.xchart.VectorGraphicsEncoder;
import com.xeiam.xchart.XChartPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.dataset.api.Instance;
import org.openide.util.Exceptions;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class CliPlot<E extends Instance, C extends Cluster<E>> extends JPanel {

    private static final long serialVersionUID = -8631119859003411411L;

    private int markerSize = 10;
    private Chart currChart;
    private XChartPanel xchart;
    private boolean simple = false;

    public CliPlot() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setSize(new Dimension(800, 600));
    }

    /**
     * Updating chart might take a while, therefore it's safer to preform update
     * in EDT
     *
     * @param clustering
     */
    public void setClustering(final Clustering<E, C> clustering) {

        removeAll();

        add(clusteringPlot(clustering),
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.NORTHWEST,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        revalidate();
        validate();
        repaint();

    }

    private JPanel clusteringPlot(final Clustering<E, C> clustering) {
        int attrX = 0;
        int attrY = 1;

        Chart chart = new Chart(getWidth(), getHeight());
        StyleManager sm = chart.getStyleManager();
        sm.setChartType(StyleManager.ChartType.Scatter);

        sm.setChartTitleVisible(false);
        if (simple) {
            // Customize Chart
            sm.setLegendVisible(false);
            sm.setAxisTitlesVisible(false);
            sm.setAxisTitlePadding(0);
            sm.setChartBackgroundColor(Color.WHITE);
            sm.setPlotBorderVisible(false);
            sm.setAxisTicksVisible(false);
        } else {
            sm.setLegendPosition(StyleManager.LegendPosition.OutsideE);
        }

        sm.setMarkerSize(markerSize);

        //update reference to current chart
        this.currChart = chart;

        for (Cluster<E> clust : clustering) {
            Series s = chart.addSeries(clust.getName(), clust.attrCollection(attrX), clust.attrCollection(attrY));
            s.setMarkerColor(clust.getColor());
        }
        xchart = new XChartPanel(chart);

        return xchart;
    }

    public void setSimpleMode(boolean b) {
        this.simple = b;
    }

    public void setClusterings(final Clustering<E, C> clusteringA, final Clustering<E, C> clusteringB) {

        removeAll();

        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0);

        // Add plot to Swing component
        add(clusteringPlot(clusteringA), c);
        c.gridx = 1;
        add(clusteringPlot(clusteringB), c);
        revalidate();
        validate();
        repaint();

    }

    public int getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(int markerSize) {
        this.markerSize = markerSize;
        revalidate();
        validate();
        repaint();
    }

    /**
     * Translate selected area into real values used in the dataset. Currently
     * only rectangular selection is supported
     *
     * @param shape
     * @return
     */
    public Rectangle.Double tranlateSelection(Shape shape) {
        if (currChart != null) {
            return currChart.translateSelection(shape.getBounds());
        }
        throw new RuntimeException("current chart not set");
    }

    public Point2D posOnCanvas(double x, double y) {
        if (currChart != null) {
            return currChart.positionOnCanvas(x, y);
        }
        throw new RuntimeException("current chart not set");
    }

    public Rectangle.Double plotArea() {
        if (currChart != null) {
            return currChart.getPlotArea();
        }
        throw new RuntimeException("current chart not set");
    }

    public void saveFile(File file, String format) {
        try {
            if (format == null) {
                BitmapEncoder.saveBitmap(currChart, file.getCanonicalPath(), BitmapEncoder.BitmapFormat.PNG);
            } else if (format.equals("jpg")) {
                BitmapEncoder.saveJPGWithQuality(currChart, BitmapEncoder.addFileExtension(file.getCanonicalPath(), BitmapEncoder.BitmapFormat.JPG), 1.0f);
            } else if (format.equals("png")) {
                BitmapEncoder.saveBitmap(currChart, file.getCanonicalPath(), BitmapEncoder.BitmapFormat.PNG);
            } else if (format.equals("*.bmp,*.BMP")) {
                BitmapEncoder.saveBitmap(currChart, file.getCanonicalPath(), BitmapEncoder.BitmapFormat.BMP);
            } else if (format.equals("gif")) {
                BitmapEncoder.saveBitmap(currChart, file.getCanonicalPath(), BitmapEncoder.BitmapFormat.GIF);
            } else if (format.equals("*.svg,*.SVG")) {
                VectorGraphicsEncoder.saveVectorGraphic(currChart, file.getCanonicalPath(), VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
            } else if (format.equals("*.eps,*.EPS")) {
                VectorGraphicsEncoder.saveVectorGraphic(currChart, file.getCanonicalPath(), VectorGraphicsEncoder.VectorGraphicsFormat.EPS);
            } else if (format.equals("*.pdf,*.PDF")) {
                VectorGraphicsEncoder.saveVectorGraphic(currChart, file.getCanonicalPath(), VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
            } else {
                throw new RuntimeException("format '" + format + "' is not supported");
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public boolean hasChart() {
        return currChart != null;
    }

}
