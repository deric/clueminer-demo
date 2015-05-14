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

import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public interface DatasetViewer {

    void addClusteringListener(ClusteringListener listener);

    void fireClusteringChanged(Clustering clust);

    Dataset<? extends Instance> getDataset();

    void setDataset(Dataset<? extends Instance> dataset);

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

    void setAlgorithm(ClusteringAlgorithm alg);

    ClusteringAlgorithm getAlgorithm();

    void setProperties(Props props);

    void dataChanged(String datasetName);

    String[] getDatasets();

}
