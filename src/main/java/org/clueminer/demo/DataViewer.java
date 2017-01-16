/*
 * Copyright (C) 2011-2017 clueminer.org
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
import org.clueminer.data.DataLoader;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.demo.gui.DendroWrapper;
import org.clueminer.demo.gui.ScatterViewer;
import org.clueminer.demo.gui.StatusPanel;
import org.clueminer.demo.gui.ViewerSettings;

/**
 *
 * @author deric
 */
public class DataViewer extends BaseFrame {

    private static final long serialVersionUID = 1458227382306409023L;

    private ScatterViewer plot;
    private ViewerSettings settings;
    private boolean showDendro = true;
    private DendroWrapper dendro;

    public DataViewer() {
        this.title = "Data viewer";
        initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new DataViewer().showInFrame();
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

        plot = new ScatterViewer(loadDatasets());

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
        settings = new ViewerSettings<>(plot, status);
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
        c.weightx = 0.8; //ratio for filling the frame space
        c.gridx = 0;
        gbl.setConstraints((Component) plot, c);
        this.add((Component) plot, c);
        setVisible(true);
    }

}
