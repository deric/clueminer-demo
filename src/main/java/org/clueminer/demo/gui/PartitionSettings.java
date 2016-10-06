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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.clueminer.chameleon.Chameleon;
import org.clueminer.clustering.api.factory.MergeEvaluationFactory;
import org.clueminer.graph.api.GraphStorageFactory;
import org.clueminer.neighbor.KnnFactory;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 */
public class PartitionSettings extends JPanel {

    private static final long serialVersionUID = -5051188335453344618L;
    private JTextField tfK;
    private JCheckBox chckAutoK;
    private JTextField tfMaxP;
    private JTextField tfAlpha;
    private JCheckBox chckAutoMaxP;
    private JCheckBox chckPartition;
    private JComboBox<String> comboSimilarity;
    private JComboBox<String> comboKnn;
    private JComboBox<String> graphStorage;

    public PartitionSettings() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0.1;
        c.weighty = 1.0;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        tfK = new JTextField("10", 10);
        chckAutoK = new JCheckBox("auto", true);
        tfK.setEnabled(false);
        add(new JLabel("k:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        add(tfK, c);
        c.gridx = 2;
        add(chckAutoK, c);
        chckAutoK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfK.setEnabled(!chckAutoK.isSelected());
            }
        });

        c.gridy++;
        c.gridx = 0;
        tfMaxP = new JTextField("10", 10);
        chckAutoMaxP = new JCheckBox("auto", true);
        tfMaxP.setEnabled(false);
        add(new JLabel("max. partition:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        add(tfMaxP, c);
        c.gridx = 2;
        add(chckAutoMaxP, c);
        chckAutoMaxP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfMaxP.setEnabled(!chckAutoMaxP.isSelected());
            }
        });

        c.gridy++;
        c.gridx = 0;
        add(new JLabel("similarity:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        comboSimilarity = new JComboBox(initSimilarity());
        add(comboSimilarity, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0.9;
        chckPartition = new JCheckBox("run partitioning", true);
        add(chckPartition, c);

        c.gridy++;
        c.gridx = 0;
        tfAlpha = new JTextField("255", 10);
        add(new JLabel("alpha:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        add(tfAlpha, c);

        c.gridy++;
        c.gridx = 0;
        add(new JLabel("k-nn search:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        comboKnn = new JComboBox(initKnn());
        add(comboKnn, c);

        c.gridy++;
        c.gridx = 0;
        add(new JLabel("graph store:"), c);
        c.gridx = 1;
        c.weightx = 0.9;
        graphStorage = new JComboBox(GraphStorageFactory.getInstance().getProvidersArray());
        graphStorage.setSelectedItem("Adjacency list graph");
        add(graphStorage, c);

    }

    public Props getParams() {
        Props props = new Props();
        if (!chckAutoK.isSelected()) {
            props.put(Chameleon.K, Integer.valueOf(tfK.getText()));
        }
        if (!chckAutoMaxP.isSelected()) {
            props.put(Chameleon.MAX_PARTITION, Integer.valueOf(tfMaxP.getText()));
        }
        props.put(Chameleon.SIM_MEASURE, comboSimilarity.getSelectedItem());
        if (!chckPartition.isSelected()) {
            props.put("skip_partition", true);
        }
        props.putInt("alpha", Integer.valueOf(tfAlpha.getText()));
        props.put(KnnFactory.KNN_SEARCH, comboKnn.getSelectedItem());
        props.put(Chameleon.GRAPH_STORAGE, graphStorage.getSelectedItem());

        return props;
    }

    private Object[] initSimilarity() {
        return MergeEvaluationFactory.getInstance().getProvidersArray();
    }

    private Object[] initKnn() {
        return KnnFactory.getInstance().getProvidersArray();
    }

}
