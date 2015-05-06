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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.demo.data.DataLoader;
import org.clueminer.demo.gui.ScatterWrapper;
import org.clueminer.demo.gui.SettingsPanel;

/**
 *
 * @author deric
 */
public class Demo2D extends JPanel {

    private ScatterWrapper plot;
    private SettingsPanel settings;

    public Demo2D() {
        setPreferredSize(new Dimension(800, 600));
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

    public String getTitle() {
        return "Clustering demo";
    }

    protected JFrame showInFrame() {
        JFrame frame = new JFrame(getTitle());
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));
        //frame.setSize(getPreferredSize());
        frame.setVisible(true);
        return frame;
    }

    private DataProvider loadDatasets() {
        Map<String, String> datasets = new HashMap<>();
        datasets.put("spiralsquare", "arff");
        datasets.put("donut1", "arff");
        datasets.put("smile1", "arff");

        return new DataLoader(datasets);
    }

    private void initComponents() {
        setSize(800, 600);

        plot = new ScatterWrapper(loadDatasets());

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

        settings = new SettingsPanel(plot);
        gbl.setConstraints(settings, c);
        add(settings, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weightx = c.weighty = 8.0; //ratio for filling the frame space


        gbl.setConstraints((Component) plot, c);
        this.add((Component) plot, c);
        setVisible(true);

    }

}
