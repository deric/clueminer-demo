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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.EvaluationTable;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.factory.Clusterings;
import org.clueminer.colors.ColorBrewer;
import org.clueminer.dataset.api.ColorGenerator;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import static org.clueminer.demo.gui.AbstractClusteringViewer.RP;
import org.clueminer.dendrogram.DataProviderMap;
import org.clueminer.eval.utils.HashEvaluationTable;
import org.clueminer.scatter.ScatterPlot;
import org.clueminer.utils.Props;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class ScatterViewer<E extends Instance, C extends Cluster<E>>
        extends AbstractClusteringViewer<E, C> implements TaskListener, DatasetViewer<E, C> {

    private static final long serialVersionUID = -8355392013651815767L;

    private ScatterPlot viewer;
    private Clustering<E, C> clust;
    private ColorGenerator cg = new ColorBrewer();

    public ScatterViewer(Map<String, Dataset<? extends Instance>> data) {
        this(new DataProviderMap(data));
    }

    public ScatterViewer(DataProvider provider) {
        super(provider);
    }

    @Override
    protected void initComponets() {
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

    /**
     * Instead of running real clustering we'll display dataset with class
     * labels
     *
     * @param params
     */
    @Override
    public void execute(final Props params) {

        task = RP.post(new Runnable() {

            @Override
            public void run() {
                fireClusteringStarted(dataset, params);
                clust = goldenClustering(dataset);

            }

        });
        task.addTaskListener(this);
    }

    private Clustering goldenClustering(Dataset<? extends Instance> dataset) {
        SortedSet set = dataset.getClasses();
        Clustering golden = Clusterings.newList();

        /* if (set.size() == 0) {
         return golden;
         }*/
        //golden.lookupAdd(dataset);
        EvaluationTable evalTable = new HashEvaluationTable(golden, dataset);
        golden.lookupAdd(evalTable);
        HashMap<Object, Integer> map = new HashMap<>(set.size());
        Object obj;
        Iterator it = set.iterator();
        int i = 0;
        Cluster c;
        cg.reset();
        while (it.hasNext()) {
            obj = it.next();
            c = golden.createCluster(i);
            c.setAttributes(dataset.getAttributes());
            c.setColor(cg.next());
            map.put(obj, i++);
        }

        int assign;

        for (Instance inst : dataset) {
            if (map.containsKey(inst.classValue())) {
                assign = map.get(inst.classValue());
                c = golden.get(assign);
            } else {
                c = golden.createCluster(i);
                c.setAttributes(dataset.getAttributes());
                map.put(inst.classValue(), i++);
            }
            c.add(inst);
        }

        return golden;
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

    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        //
    }

    @Override
    public void clusteringChanged(Clustering<E, C> clust) {
        if (clust != null) {
            viewer.setClustering(clust);
        }
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //
    }

}
