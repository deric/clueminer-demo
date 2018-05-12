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
package org.clueminer.demo;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.SwingUtilities;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.data.DataLoader;
import org.clueminer.demo.gui.MatrixWrapper;
import org.clueminer.demo.gui.SettingsPanel;
import org.clueminer.demo.gui.StatusPanel;

/**
 *
 * @author deric
 */
public class RealWorldDemo extends BaseFrame {

    private static final long serialVersionUID = 8659317701969970764L;

    private MatrixWrapper plot;
    private SettingsPanel settings;

    public RealWorldDemo() {
        initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new RealWorldDemo().showInFrame();
            }
        });
    }

    private DataProvider loadDatasets() {
        return DataLoader.createLoader("datasets", "real-world");
    }

    private void initComponents() {
        setSize(800, 600);

        plot = new MatrixWrapper(loadDatasets());

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
        c.weightx = c.weighty = 8.0; //ratio for filling the frame space

        gbl.setConstraints((Component) plot, c);
        this.add((Component) plot, c);
        setVisible(true);
    }

}
