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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.gui.ClusteringDialog;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class ViewerSettings<E extends Instance, C extends Cluster<E>> extends JPanel implements ClusteringListener<E, C> {

    private static final long serialVersionUID = 4694033662557233989L;

    private JComboBox dataBox;
    private final DatasetViewer panel;
    private ClusteringDialog optPanel;
    protected final transient EventListenerList controlListeners = new EventListenerList();

    public ViewerSettings(DatasetViewer panel, StatusPanel status) {
        this.panel = panel;
        panel.addClusteringListener(this);
        controlListeners.add(ControlListener.class, status);
        initComponents();
        setDatasets(panel.getDatasets());
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        dataBox = new JComboBox();
        dataBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.dataChanged((String) dataBox.getSelectedItem());
                execute();
            }
        });
        add(dataBox);

    }

    private void execute() {
        Props p = getProps();
        panel.execute(p);
    }

    public final void setDatasets(String[] datasets) {
        for (String str : datasets) {
            dataBox.addItem(str);
        }
    }

    public Props getProps() {

        if (optPanel != null) {
            return optPanel.getParams();
        } else {
            return new Props();
        }
    }

    @Override
    public void clusteringChanged(Clustering clust) {
        dataBox.setEnabled(true);
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //not much to do
    }

    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        //TODO: disable controls?
        dataBox.setEnabled(false);
    }

    public void fireBatchStarted(Dataset<? extends Instance> dataset, Props param) {
        for (ControlListener listener : controlListeners.getListeners(ControlListener.class)) {
            listener.batchStarted(dataset, param);
        }
    }

}
