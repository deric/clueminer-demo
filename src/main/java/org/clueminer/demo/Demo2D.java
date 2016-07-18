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
package org.clueminer.demo;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.data.DataLoader;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Instance;
import org.clueminer.demo.gui.DendroWrapper;
import org.clueminer.demo.gui.ScatterWrapper;
import org.clueminer.demo.gui.SettingsPanel;
import org.clueminer.demo.gui.StatusPanel;

/**
 * Simple GUI for displaying clustering results
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class Demo2D<E extends Instance, C extends Cluster<E>> extends BaseFrame {

    private static final long serialVersionUID = 1458227382306409023L;

    private ScatterWrapper plot;
    private SettingsPanel settings;
    private boolean showDendro = true;
    private DendroWrapper<E, C> dendro;
    private JList<String> datasets;

    public Demo2D() {
        initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Demo2D().showInFrame();
            }
        });
    }

    /**
     * Load all resources from classpath
     *
     * @return
     */
    private DataProvider loadDatasets() {
        return DataLoader.createLoader("datasets", "artificial");
    }

    private void initComponents() {
        setSize(800, 600);
        DataProvider data = loadDatasets();

        plot = new ScatterWrapper(data);

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = c.weighty = 0.2; //no fill while resize

        status = new StatusPanel(plot);
        settings = new SettingsPanel(plot, status);
        gbl.setConstraints(settings, c);
        add(settings, c);
        //status bar
        c.gridy = 2;

        add(status, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weighty = 8.0;

        //datasets
        JScrollPane dataScrollPane = new JScrollPane();
        datasets = new JList<>(data.getDatasetNames());
        //only one item can be selected at the time
        datasets.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        dataScrollPane.setViewportView(datasets);
        datasets.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                plot.dataChanged(datasets.getSelectedValue());
                settings.updateAlgorithm();
                settings.execute();
            }
        });

        JSplitPane plotPane = new JSplitPane();
        plotPane.setOneTouchExpandable(true);
        plotPane.setDividerLocation(100);
        plotPane.setLeftComponent(dataScrollPane);
        plotPane.setRightComponent(plot);

        if (showDendro) {
            c.weightx = 0.5;
            dendro = new DendroWrapper<>(plot, status);
            plot.addClusteringListener(dendro);
            c.gridx = 1;

            JSplitPane splitPane = new JSplitPane();
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(700);
            splitPane.setLeftComponent(plotPane);
            splitPane.setRightComponent(dendro);
            c.gridx = 0;
            add(splitPane, c);
        } else {
            c.gridx = 0;
            c.weightx = 0.8; //ratio for filling the frame space
            gbl.setConstraints((Component) plotPane, c);
            this.add((Component) plotPane, c);
        }
        setVisible(true);
        //select some dataset
        datasets.setSelectedValue("aggregation", true);
    }

}
