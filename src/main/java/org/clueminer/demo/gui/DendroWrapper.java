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
package org.clueminer.demo.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.dendrogram.DendrogramMapping;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dgram.DgViewer;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class DendroWrapper extends JPanel implements ClusteringListener {

    private DgViewer viewer;

    public DendroWrapper(DatasetViewer panel) {
        initComponets();
        viewer.addClusteringListener(panel);
    }

    private void initComponets() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();

        viewer = new DgViewer();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weightx = c.weighty = 1.0; //ratio for filling the frame space
        gbl.setConstraints((Component) viewer, c);
        this.add((Component) viewer, c);
        setVisible(true);
    }

    @Override
    public void clusteringStarted(Dataset<? extends Instance> dataset, Props params) {

    }

    @Override
    public void clusteringChanged(Clustering clust) {
        if (clust != null) {
            DendrogramMapping dm = clust.getLookup().lookup(DendrogramMapping.class);
            if (dm != null) {
                viewer.setDataset(dm);
                viewer.setVisible(true);
            } else {
                //no hierarchical data
                viewer.setVisible(false);
            }
        }
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        if (hclust != null) {
            viewer.setDataset(hclust.getDendrogramMapping());
        }
    }

}
