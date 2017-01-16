/*
 * Copyright (C) 2011-2017 clueminer.org
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.dataset.api.Instance;
import org.math.plot.Plot3DPanel;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class Scatter3DPlot<E extends Instance, C extends Cluster<E>> extends JPanel {

    private static final long serialVersionUID = 8065053854767902112L;

    private Plot3DPanel plot;

    public Scatter3DPlot() {
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
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
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
        });
    }

    private JPanel clusteringPlot(final Clustering<E, C> clustering) {
        plot = new Plot3DPanel();

        double[] x, y, z;
        int i;
        for (Cluster<E> clust : clustering) {
            x = new double[clust.size()];
            y = new double[clust.size()];
            z = new double[clust.size()];
            i = 0;
            for (E inst : clust) {
                x[i] = inst.value(0);
                y[i] = inst.value(1);
                z[i] = inst.value(2);
                i++;
            }
            plot.addScatterPlot(clust.getName(), clust.getColor(), x, y, z);
        }

        return plot;
    }

}
