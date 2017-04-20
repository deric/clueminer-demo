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

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.Executor;
import org.clueminer.colors.ColorBrewer;
import org.clueminer.dataset.api.ColorGenerator;
import org.clueminer.dataset.api.ColorGeneratorFactory;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;
import org.openide.util.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public abstract class AbstractClusteringViewer<E extends Instance, C extends Cluster<E>> extends JPanel implements DatasetViewer<E, C> {

    private static final long serialVersionUID = -7047730532984328395L;

    protected ClusteringAlgorithm<E, C> algorithm;
    protected Dataset<E> dataset;
    protected DataProvider<E> dataProvider;
    protected Props properties;
    protected Executor exec;
    protected ColorGenerator cg;
    protected final transient EventListenerList clusteringListeners = new EventListenerList();
    protected static RequestProcessor RP = new RequestProcessor("Clustering");
    protected RequestProcessor.Task task;
    protected Clustering<E, C> clust;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractClusteringViewer.class);
    public static final String CG_CONF = "color_generator";

    public AbstractClusteringViewer(DataProvider<E> provider) {
        dataProvider = provider;
        properties = new Props();
        setDataset(dataProvider.first());

        cg = new ColorBrewer();
        exec = new ClusteringExecutorCached();
        ClusteringFactory cf = ClusteringFactory.getInstance();
        if (cf.hasProvider("k-means")) {
            setAlgorithm(cf.getProvider("k-means"));
            properties.putInt("k", 3);
        } else {
            setAlgorithm(cf.getDefault());
        }
        //options.setDatasets(dataProvider.getDatasetNames());
        initComponets();
    }

    public void updateConfiguration(Props params) {
        if (params.containsKey(CG_CONF)) {
            String provider = params.get(CG_CONF);
            cg = ColorGeneratorFactory.getInstance().getProvider(provider);
        }
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
    public Dataset<E> getDataset() {
        return dataset;
    }

    @Override
    public final void setDataset(Dataset<E> dataset) {
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

    public abstract void datasetChanged(Dataset<E> dataset);

    @Override
    public void dataChanged(String datasetName) {
        setDataset(dataProvider.getDataset(datasetName));
        LOG.info("dataset changed to {}: {}", datasetName, System.identityHashCode(getDataset()));
        datasetChanged(dataset);
    }

    @Override
    public void addClusteringListener(ClusteringListener<E, C> listener) {
        clusteringListeners.add(ClusteringListener.class, listener);
    }

    @Override
    public void fireClusteringChanged(Clustering<E, C> clust) {
        for (ClusteringListener listener : clusteringListeners.getListeners(ClusteringListener.class)) {
            listener.clusteringChanged(clust);
        }
    }

    public void fireClusteringStarted(Dataset<E> dataset, Props param) {
        for (ClusteringListener listener : clusteringListeners.getListeners(ClusteringListener.class)) {
            listener.clusteringStarted(dataset, param);
        }
    }

    @Override
    public void abort() {
        if (task != null) {
            //it's hard to remove running task, we have to perform complete shutdown
            RP.shutdown();
            //replace request processor
            RP = new RequestProcessor("Clustering");
        }
        //no result yet
        fireClusteringChanged(null);
    }

    @Override
    public Clustering<E, C> getClustering() {
        return clust;
    }

    @Override
    public void setDataProvider(DataProvider provider) {
        this.dataProvider = provider;
    }

    @Override
    public void setColorGenerator(ColorGenerator cg) {
        this.cg = cg;
        colorGeneratorChanged(cg);
    }

    public abstract void colorGeneratorChanged(ColorGenerator cg);

}
