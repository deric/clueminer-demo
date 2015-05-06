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
import java.util.Map;
import javax.swing.JPanel;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.AgglomerativeClustering;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.Executor;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dendrogram.DataProviderMap;
import org.clueminer.distance.api.DistanceFactory;
import org.clueminer.distance.api.DistanceMeasure;
import org.clueminer.report.MemInfo;
import org.clueminer.scatter.ScatterPlot;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class ScatterWrapper extends JPanel {

    protected ClusteringAlgorithm algorithm;
    private Dataset<? extends Instance> dataset;
    private DataProvider dataProvider;
    private ScatterPlot viewer;
    protected Props properties;
    private Executor exec;

    public ScatterWrapper(Map<String, Dataset<? extends Instance>> data) {
        this(new DataProviderMap(data));
    }

    public ScatterWrapper(DataProvider provider) {
        dataProvider = provider;
        properties = new Props();
        setDataset(dataProvider.first());
        exec = new ClusteringExecutorCached();
        algorithm = ClusteringFactory.getInstance().getDefault();
        //options.setDatasets(dataProvider.getDatasetNames());
        initComponets();
    }

    private void initComponets() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();

        viewer = new ScatterPlot();
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

    public void setClustering(Clustering clusters) {
        viewer.setClustering(clusters);
    }

    void dataChanged(String datasetName) {
        setDataset(dataProvider.getDataset(datasetName));
        System.out.println("dataset changed to " + datasetName + ": " + System.identityHashCode(getDataset()));
        if (algorithm != null) {
            execute();
        }
    }

    public ClusteringAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(ClusteringAlgorithm alg) {
        this.algorithm = alg;
    }

    public void execute() {
        Props params = getProperties().copy();
        execute(params);
    }

    public void execute(Props params) {
        MemInfo memInfo = new MemInfo();

        DistanceFactory df = DistanceFactory.getInstance();
        DistanceMeasure func = df.getProvider("Euclidean");
        if (algorithm == null) {
            throw new RuntimeException("no algorithm was set");
        }
        params.put("name", getAlgorithm().getName());
        algorithm.setDistanceFunction(func);

        exec.setAlgorithm((AgglomerativeClustering) algorithm);
        Clustering<? extends Cluster> clust = exec.clusterRows(dataset, params);
        memInfo.report();

        viewer.setClustering(clust);


        validate();
        revalidate();
        repaint();
    }

    public Dataset<? extends Instance> getDataset() {
        return dataset;
    }

    public final void setDataset(Dataset<? extends Instance> dataset) {
        this.dataset = dataset;
    }

    public void setProperties(Props props) {
        this.properties = props;
    }

    public Props getProperties() {
        return properties;
    }

    public String[] getDatasets() {
        return dataProvider.getDatasetNames();
    }
}
