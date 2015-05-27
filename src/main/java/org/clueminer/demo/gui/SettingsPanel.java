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
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.ExternalEvaluator;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.factory.ExternalEvaluatorFactory;
import org.clueminer.clustering.gui.ClusteringDialog;
import org.clueminer.clustering.gui.ClusteringDialogFactory;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.utils.Props;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author deric
 */
public class SettingsPanel extends JPanel implements ClusteringListener {

    private static final long serialVersionUID = 4694033662557233989L;

    private JButton btnOptions;
    private JComboBox dataBox;
    private ClusteringFactory cf;
    private JComboBox algBox;
    private JComboBox validationBox;
    private final DatasetViewer panel;
    private ClusteringDialog optPanel;
    private ExternalEvaluator evaluator;
    private final HashMap<ClusteringAlgorithm, JPanel> optPanels;
    private StatusPanel status;

    public SettingsPanel(DatasetViewer panel, StatusPanel status) {
        this.panel = panel;
        optPanels = new HashMap<>();
        this.status = status;
        panel.addClusteringListener(this);
        initComponents();
        algBox.setSelectedItem("k-means (MacQueen)");
        setDatasets(panel.getDatasets());

    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        dataBox = new JComboBox();
        dataBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.dataChanged((String) dataBox.getSelectedItem());
                panel.execute(getProps());
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
                    panel.execute(getProps());
                }
            }
        });
        add(algBox);

        btnOptions = new JButton("Settings");
        btnOptions.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DialogDescriptor dd = new DialogDescriptor(updateUI(getAlgorithm()), "Settings");
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    updateAlgorithm();

                    panel.execute(getProps());
                }
            }
        });
        add(btnOptions);

        validationBox = new JComboBox(ExternalEvaluatorFactory.getInstance().getProvidersArray());
        validationBox.setSelectedItem("NMI");
        updateEvaluator("NMI");
        validationBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String validator = (String) validationBox.getSelectedItem();
                if (!validator.equals(evaluator.getName())) {
                    updateEvaluator(validator);
                    status.setEvaluator(evaluator);
                }
            }
        });

        add(new JLabel("Validation:"));

        add(validationBox);
    }

    private void updateEvaluator(String validator) {
        evaluator = ExternalEvaluatorFactory.getInstance().getProvider(validator);
    }

    private JPanel updateUI(ClusteringAlgorithm alg) {
        optPanel = null;
        for (ClusteringDialog dlg : ClusteringDialogFactory.getInstance().getAll()) {
            if (dlg.isUIfor(alg)) {
                optPanel = dlg;
                if (!optPanels.containsKey(alg)) {
                    optPanels.put(alg, dlg.getPanel());
                }
                return dlg.getPanel();
            }
        }
        //last resort
        return new JPanel();
    }

    public void updateAlgorithm() {
        updateUI(getAlgorithm());
    }

    public final void setDatasets(String[] datasets) {
        for (String str : datasets) {
            dataBox.addItem(str);
        }
    }

    public ClusteringAlgorithm getAlgorithm() {
        String algName = (String) algBox.getSelectedItem();
        ClusteringAlgorithm algorithm = ClusteringFactory.getInstance().getProvider(algName);
        return algorithm;
    }

    public void selectAlgorithm(String algorithm) {
        algBox.setSelectedItem(algorithm);
    }

    public Props getProps() {
        updateUI(getAlgorithm());

        if (optPanel != null) {
            return optPanel.getParams();
        } else {
            return new Props();
        }
    }

    @Override
    public void clusteringChanged(Clustering clust) {
        algBox.setEnabled(true);
        dataBox.setEnabled(true);
        btnOptions.setEnabled(true);
        validationBox.setEnabled(true);
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //not much to do
    }

    @Override
    public void clusteringStarted(Dataset<? extends Instance> dataset, Props params) {
        //TODO: disable controls?
        algBox.setEnabled(false);
        dataBox.setEnabled(false);
        btnOptions.setEnabled(false);
        validationBox.setEnabled(false);
    }

}
