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

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class StatusPanel extends JPanel implements ClusteringListener {

    private JProgressBar progressBar;
    private JLabel lbStatus;

    public StatusPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        lbStatus = new JLabel("");
        add(lbStatus);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        add(progressBar);
    }

    @Override
    public void clusteringChanged(Clustering clust) {
        lbStatus.setText("");
        progressBar.setVisible(false);
        //
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //
    }

    @Override
    public void clusteringStarted(Dataset<? extends Instance> dataset, Props params) {
        lbStatus.setText("Clustering " + dataset.getName() + " with " + params.get("algorithm") + " ...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        //
    }

}
