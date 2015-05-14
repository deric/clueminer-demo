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

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.Executor;
import org.clueminer.colors.ColorBrewer;
import org.clueminer.dataset.api.ColorGenerator;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;
import org.openide.util.RequestProcessor;

/**
 *
 * @author deric
 */
public abstract class AbstractClusteringViewer extends JPanel implements DatasetViewer {

    protected ClusteringAlgorithm algorithm;
    protected Dataset<? extends Instance> dataset;
    protected DataProvider dataProvider;
    protected Props properties;
    protected Executor exec;
    protected ColorGenerator cg;
    protected final transient EventListenerList clusteringListeners = new EventListenerList();
    protected static final RequestProcessor RP = new RequestProcessor("Clustering");
    protected RequestProcessor.Task task;

    public AbstractClusteringViewer(DataProvider provider) {
        dataProvider = provider;
        properties = new Props();
        setDataset(dataProvider.first());
        exec = new ClusteringExecutorCached();
        setAlgorithm(ClusteringFactory.getInstance().getDefault());
        cg = new ColorBrewer();
        //options.setDatasets(dataProvider.getDatasetNames());
        initComponets();
    }

    protected abstract void initComponets();

    @Override
    public ClusteringAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public final void setAlgorithm(ClusteringAlgorithm alg) {
        this.algorithm = alg;
        alg.setColorGenerator(cg);
    }

    public void execute() {
        Props params = getProperties().copy();
        execute(params);
    }

    @Override
    public Dataset<? extends Instance> getDataset() {
        return dataset;
    }

    @Override
    public final void setDataset(Dataset<? extends Instance> dataset) {
        this.dataset = dataset;
    }

    @Override
    public void setProperties(Props props) {
        this.properties = props;
    }

    public Props getProperties() {
        return properties;
    }

    @Override
    public String[] getDatasets() {
        return dataProvider.getDatasetNames();
    }

    public void dataChanged(String datasetName) {
        setDataset(dataProvider.getDataset(datasetName));
        System.out.println("dataset changed to " + datasetName + ": " + System.identityHashCode(getDataset()));
    }

    @Override
    public void addClusteringListener(ClusteringListener listener) {
        clusteringListeners.add(ClusteringListener.class, listener);
    }

    @Override
    public void fireClusteringChanged(Clustering clust) {
        for (ClusteringListener listener : clusteringListeners.getListeners(ClusteringListener.class)) {
            listener.clusteringChanged(clust);
        }
    }

    public void fireClusteringStarted(Dataset<? extends Instance> dataset, Props param) {
        for (ClusteringListener listener : clusteringListeners.getListeners(ClusteringListener.class)) {
            listener.clusteringStarted(dataset, param);
        }
    }

    @Override
    public void abort() {
        if (task != null) {
            task.cancel();
        }
        //no result yet
        fireClusteringChanged(null);
    }

}
