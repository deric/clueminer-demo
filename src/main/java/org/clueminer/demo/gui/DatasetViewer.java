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

import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.dataset.api.ColorGenerator;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public interface DatasetViewer<E extends Instance, C extends Cluster<E>> extends ClusteringListener<E, C> {

    void addClusteringListener(ClusteringListener<E, C> listener);

    void fireClusteringChanged(Clustering<E, C> clust);

    Dataset<E> getDataset();

    void setDataset(Dataset<E> dataset);

    /**
     * Start clustering
     *
     * @param params
     */
    void execute(final Props params);

    /**
     * Abort current clustering, if any
     */
    void abort();

    void setAlgorithm(ClusteringAlgorithm<E, C> alg);

    ClusteringAlgorithm<E, C> getAlgorithm();

    void setProperties(Props props);

    void dataChanged(String datasetName);

    String[] getDatasets();

    /**
     * Get current clustering result
     *
     * @return
     */
    Clustering<E, C> getClustering();

    /**
     * Assign given label to selected items
     *
     * @param label
     */
    void assignLabelToSelection(String label);

    void setDataProvider(DataProvider provider);

    void setColorGenerator(ColorGenerator cg);
}
