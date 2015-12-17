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
package org.clueminer.demo;

import com.google.common.collect.ImmutableMap;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.clueminer.clustering.aggl.HC;
import org.clueminer.clustering.aggl.HCLW;
import org.clueminer.clustering.aggl.linkage.SingleLinkage;
import org.clueminer.clustering.api.AgglParams;
import org.clueminer.clustering.api.dendrogram.DendrogramMapping;
import org.clueminer.clustering.api.dendrogram.OptimalTreeOrder;
import org.clueminer.clustering.order.MOLO;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.demo.gui.HacDendroPanel;
import org.clueminer.dendrogram.DendroPanel;
import org.clueminer.fixtures.clustering.FakeDatasets;
import org.clueminer.utils.Props;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author deric
 */
public class ClusteringFight extends JFrame {

    private static final RequestProcessor RP = new RequestProcessor("non-interruptible tasks", 1, false);
    private static final long serialVersionUID = -6891327419249057159L;
    private DendroPanel panel1;
    private DendroPanel panel2;

    public ClusteringFight() throws Exception {
        super("ClusteringFight Frame");
        initComponents();

        RP.execute(new Runnable() {
            @Override
            public void run() {
                Props props = new Props();
                props.put(AgglParams.LINKAGE, SingleLinkage.name);

                panel1.setProperties(props);
                panel2.setProperties(props);

                panel1.setAlgorithm(new HC());
                panel2.setAlgorithm(new HCLW());
                DendrogramMapping res1 = panel1.execute();
                DendrogramMapping res2 = panel2.execute();
                OptimalTreeOrder order = new MOLO();

                order.optimize(res1.getRowsResult());
                order.optimize(res1.getColsResult());
                panel1.viewer.fireColumnsMappingChanged(order, res1.getColsResult());
                panel1.viewer.fireRowMappingChanged(order, res1.getRowsResult());

                //order.optimize(res2.getRowsResult(), false);
                order.optimize(res2.getRowsResult());
                order.optimize(res2.getColsResult());
                panel2.viewer.fireColumnsMappingChanged(order, res2.getColsResult());
                panel2.viewer.fireRowMappingChanged(order, res2.getRowsResult());
            }
        });

        //dendro.setDataset(new ClusterDataset(generateRandom(15, 5)));
        setVisible(true);
    }

    private void initComponents() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();

        ImmutableMap<String, Dataset<? extends Instance>> map = new ImmutableMap.Builder<String, Dataset<? extends Instance>>()
                .put("school", FakeDatasets.schoolData())
                .put("iris", FakeDatasets.irisDataset())
                .put("US arrests", FakeDatasets.usArrestData())
                .put("glass", FakeDatasets.glassDataset())
                .build();

        panel1 = new HacDendroPanel(map);
        panel2 = new HacDendroPanel(map);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weightx = c.weighty = 1.0; //ratio for filling the frame space
        gbl.setConstraints(panel1, c);
        this.add(panel1, c);

        c.gridx = 1;
        gbl.setConstraints(panel2, c);
        this.add(panel2, c);
        setVisible(true);
    }

    // this function will be run from the EDT
    private static void createAndShowGUI() throws Exception {
        ClusteringFight hmf = new ClusteringFight();
        hmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hmf.setSize(500, 500);
        hmf.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createAndShowGUI();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
}
