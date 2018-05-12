/*
 * Copyright (C) 2011-2018 clueminer.org
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.graph.api.GraphConvertorFactory;
import org.clueminer.graph.knn.KNNGraphBuilder;
import org.clueminer.utils.Props;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Settings for visualization of Chameleon's merging process
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class SimSettings<E extends Instance, C extends Cluster<E>> extends JPanel implements ClusteringListener<E, C> {

    private static final long serialVersionUID = 4694033662557233989L;

    private JComboBox<String> dataBox;
    private JComboBox<String> graphConvertors;
    private JButton btnSettings;
    private final SimViewer panel;
    private PartitionSettings partitionPanel;
    protected final transient EventListenerList controlListeners = new EventListenerList();

    public SimSettings(SimViewer panel, StatusPanel status) {
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

        graphConvertors = new JComboBox(GraphConvertorFactory.getInstance().getProvidersArray());
        graphConvertors.setSelectedItem(KNNGraphBuilder.NAME);
        graphConvertors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphConvertorFactory gcf = GraphConvertorFactory.getInstance();
                panel.setGraphConvertor(gcf.getProvider(graphConvertors.getSelectedItem().toString()));
                panel.computeNN(panel.getDataset(), getProps());
            }
        });
        add(graphConvertors);

        btnSettings = new JButton("Settings");
        btnSettings.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (partitionPanel == null) {
                    partitionPanel = new PartitionSettings();
                }
                DialogDescriptor dd = new DialogDescriptor(partitionPanel, "Configure partitioning");
                if (!DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    //exportDialog.destroy();
                    return;
                }
                //exportDialog.destroy();

                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    panel.computeNN(panel.getDataset(), getProps());
                    execute();
                }
            }
        });
        add(btnSettings);
    }

    private void execute() {
        panel.execute(getProps());
    }

    public final void setDatasets(String[] datasets) {
        for (String str : datasets) {
            dataBox.addItem(str);
        }
    }

    public Props getProps() {
        if (partitionPanel != null) {
            return partitionPanel.getParams();
        } else {
            return new Props();
        }
    }

    @Override
    public void clusteringChanged(Clustering<E, C> clust) {
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
