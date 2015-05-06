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
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.AgglomerativeClustering;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.ClusteringListener;
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
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author deric
 */
public class ScatterWrapper extends JPanel implements TaskListener {

    private static final long serialVersionUID = -8355392013651815767L;

    protected ClusteringAlgorithm algorithm;
    private Dataset<? extends Instance> dataset;
    private DataProvider dataProvider;
    private ScatterPlot viewer;
    protected Props properties;
    private Executor exec;
    private static final RequestProcessor RP = new RequestProcessor("Clustering");
    private RequestProcessor.Task task;
    private Clustering<? extends Cluster> clust;
    private final transient EventListenerList clusteringListeners = new EventListenerList();

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

    public void execute(final Props params) {
        if (algorithm == null) {
            throw new RuntimeException("no algorithm was set");
        }
        task = RP.create(new Runnable() {

            @Override
            public void run() {
                System.out.println("algorithm: " + algorithm.getName());
                params.put("name", getAlgorithm().getName());
                System.out.println(params.toString());
                DistanceFactory df = DistanceFactory.getInstance();
                DistanceMeasure func = df.getProvider("Euclidean");
                algorithm.setDistanceFunction(func);

                MemInfo memInfo = new MemInfo();
                exec.setAlgorithm((AgglomerativeClustering) algorithm);
                clust = exec.clusterRows(dataset, params);
                memInfo.report();
                System.out.println("------");
            }

        });
        task.addTaskListener(this);
        task.schedule(0);
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

    @Override
    public void taskFinished(Task task) {
        if (clust != null && clust.size() > 0 && clust.instancesCount() > 0) {
            viewer.setClustering(clust);
            fireClusteringChanged(clust);

            validate();
            revalidate();
            repaint();
        } else {
            System.err.println("invalid clustering");
        }

    }

    public void addClusteringListener(ClusteringListener listener) {
        clusteringListeners.add(ClusteringListener.class, listener);
    }

    public void fireClusteringChanged(Clustering clust) {
        for (ClusteringListener listener : clusteringListeners.getListeners(ClusteringListener.class)) {
            listener.clusteringChanged(clust);
        }
    }
}
