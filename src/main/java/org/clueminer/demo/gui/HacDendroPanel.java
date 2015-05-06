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

import java.util.Map;
import org.clueminer.clustering.ClusteringExecutorCached;
import org.clueminer.clustering.api.AgglParams;
import org.clueminer.clustering.api.Executor;
import org.clueminer.clustering.api.dendrogram.DendrogramMapping;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dendrogram.DataProvider;
import org.clueminer.dendrogram.DendroPanel;
import org.clueminer.dgram.DgViewer;
import org.clueminer.distance.api.DistanceFactory;
import org.clueminer.distance.api.DistanceMeasure;
import org.clueminer.report.MemInfo;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class HacDendroPanel extends DendroPanel {

    private boolean debug = false;
    private DataProvider dataProvider;
    private Executor exec;

    public HacDendroPanel(Map<String, Dataset<? extends Instance>> data) {
        this(new DataProvider(data));
    }

    public HacDendroPanel(DataProvider provider) {
        dataProvider = provider;
        setDataset(dataProvider.first());
        options.setDatasets(dataProvider.getDatasetNames());
        exec = new ClusteringExecutorCached();
    }

    @Override
    public void initViewer() {
        viewer = new DgViewer();
    }

    @Override
    public DendrogramMapping execute() {
        Props params = getProperties().copy();
        return execute(params);
    }

    public DendrogramMapping execute(Props params) {
        MemInfo memInfo = new MemInfo();

        DistanceFactory df = DistanceFactory.getInstance();
        DistanceMeasure func = df.getProvider("Euclidean");
        if (algorithm == null) {
            throw new RuntimeException("no algorithm was set");
        }
        params.put("name", getAlgorithm().getName());
        algorithm.setDistanceFunction(func);

        exec.setAlgorithm(algorithm);
        DendrogramMapping dendroData = exec.clusterAll(getDataset(), params);
        memInfo.report();

        viewer.setDataset(dendroData);

        validate();
        revalidate();
        repaint();
        return dendroData;
    }

    @Override
    public void dataChanged(String datasetName) {
        setDataset(dataProvider.getDataset(datasetName));
        System.out.println("dataset changed to " + datasetName + ": " + System.identityHashCode(getDataset()));
        if (algorithm != null) {
            execute();
        }
    }

    @Override
    public String[] getDatasets() {
        return dataProvider.getDatasetNames();
    }

    @Override
    public void linkageChanged(String linkage) {
        Props params = getProperties().copy();
        params.put(AgglParams.LINKAGE, linkage);
        execute(params);
    }

    @Override
    public void cutoffChanged(String cutoff) {
        Props params = getProperties().copy();
        params.put(AgglParams.CUTOFF_STRATEGY, cutoff);
        execute(params);
    }

}
