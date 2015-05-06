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
import org.clueminer.clustering.api.ClusteringFactory;

/**
 *
 * @author deric
 */
public class SettingsPanel extends JPanel {

    private JComboBox dataBox;
    private ClusteringFactory cf;
    private JComboBox algBox;
    private final ScatterWrapper panel;

    public SettingsPanel(ScatterWrapper panel) {
        this.panel = panel;
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
            }
        });
        add(dataBox);

        cf = ClusteringFactory.getInstance();
        algBox = new JComboBox(cf.getProvidersArray());

        algBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String alg = (String) algBox.getSelectedItem();
                //if algorithm was really changed, trigger execution
                if (!alg.equals(panel.getAlgorithm().getName())) {
                    panel.setAlgorithm(cf.getProvider(alg));
                    panel.execute();
                }
            }
        });
        add(algBox);
    }

    public final void setDatasets(String[] datasets) {
        for (String str : datasets) {
            dataBox.addItem(str);
        }
    }

    public void selectAlgorithm(String algorithm) {
        algBox.setSelectedItem(algorithm);
    }

}
