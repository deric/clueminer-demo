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
package org.clueminer.demo.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringAlgorithm;
import org.clueminer.clustering.api.ClusteringFactory;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.factory.EvaluationFactory;
import org.clueminer.clustering.gui.ClusteringDialog;
import org.clueminer.clustering.gui.ClusteringDialogFactory;
import org.clueminer.clustering.gui.ClusteringExportGui;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.dendrogram.FileExportDialog;
import org.clueminer.utils.Props;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class SettingsPanel<E extends Instance, C extends Cluster<E>> extends JPanel implements ClusteringListener<E, C> {

    private static final long serialVersionUID = 4694033662557233989L;

    private JButton btnOptions;
    private ClusteringFactory cf;
    private JComboBox algBox;
    private JComboBox validationBox;
    private JSpinner spinRepeat;
    private JButton btnExport;
    private JButton btnSettings;
    private final DatasetViewer panel;
    private ClusteringDialog optPanel;
    private SettingsAdvancedPanel confPanel;
    private ClusterEvaluation evaluator;
    private final HashMap<ClusteringAlgorithm, JPanel> optPanels;
    private final StatusPanel status;
    protected final transient EventListenerList controlListeners = new EventListenerList();
    private static final Logger LOG = LoggerFactory.getLogger(SettingsPanel.class);

    public SettingsPanel(DatasetViewer panel, StatusPanel status) {
        this.panel = panel;
        optPanels = new HashMap<>();
        this.status = status;
        panel.addClusteringListener(this);
        controlListeners.add(ControlListener.class, status);
        initComponents();
        algBox.setSelectedItem("k-means");
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        cf = ClusteringFactory.getInstance();
        algBox = new JComboBox(cf.getProvidersArray());

        algBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String alg = (String) algBox.getSelectedItem();
                //if algorithm was really changed, trigger execution
                if (!alg.equals(panel.getAlgorithm().getName())) {
                    panel.setAlgorithm(cf.getProvider(alg));
                    updateAlgorithm();
                    execute();
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
                    execute();
                }
            }
        });
        add(btnOptions);

        validationBox = new JComboBox(EvaluationFactory.getInstance().getProvidersArray());
        validationBox.setSelectedItem("NMI-sqrt");
        updateEvaluator("NMI-sqrt");
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

        add(new JLabel("Repeat:"));
        spinRepeat = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinRepeat.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                //trigger new batch start
                execute();
            }
        });
        add(spinRepeat);

        btnExport = new JButton(ImageUtilities.loadImageIcon("org/clueminer/demo/save16.png", false));
        btnExport.setToolTipText("Export current results");
        add(btnExport);

        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                FileExportDialog exportDialog = new FileExportDialog();
                DialogDescriptor dd = new DialogDescriptor(exportDialog, "Export to...");
                if (!DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    //exportDialog.destroy();
                    return;
                }
                //exportDialog.destroy();

                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    ClusteringExportGui exp = exportDialog.getExporter();
                    exp.setClustering(panel.getClustering());
                    //exp.setViewer(viewer);
                    exp.export();
                }
            }
        });

        btnSettings = new JButton(ImageUtilities.loadImageIcon("org/clueminer/demo/settings16.png", false));
        btnSettings.setToolTipText("Advanced configuration");
        btnSettings.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (confPanel == null) {
                    confPanel = new SettingsAdvancedPanel();
                }

                DialogDescriptor dd = new DialogDescriptor(confPanel, "Advanced configuration");
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    execute();
                }
            }
        });
        add(btnSettings);
    }

    public void execute() {
        int i = 0;
        int repeat = (int) spinRepeat.getValue();
        Props p = getProps();
        if (repeat > 0) {
            fireBatchStarted(panel.getDataset(), p);

            while (i < repeat) {
                panel.execute(p);
                i++;
            }
        }
    }

    private void updateEvaluator(String validator) {
        evaluator = EvaluationFactory.getInstance().getProvider(validator);
    }

    private JPanel updateUI(ClusteringAlgorithm alg) {
        if (optPanel != null && optPanel.isUIfor(alg, panel.getDataset())) {
            return optPanel.getPanel();
        } else {
            LOG.debug("Algorithm: {}",alg.getClass().toString());
            for (ClusteringDialog dlg : ClusteringDialogFactory.getInstance().getAll()) {
                boolean isSuitable = dlg.isUIfor(alg, panel.getDataset());
                LOG.debug("testing dialog {}, applicable?: {}", dlg.getName(), isSuitable);
                if (isSuitable) {
                    optPanel = dlg;
                    if (!optPanels.containsKey(alg)) {
                        optPanels.put(alg, dlg.getPanel());
                    }
                    return dlg.getPanel();
                }
            }
        }
        LOG.debug("Could not find UI for algorithm {}", alg.getName());
        StringBuilder sb = new StringBuilder();
        optPanels.keySet().forEach((cAlg) -> {
            if(sb.length() > 0){
                sb.append(", ");
            }
            sb.append(cAlg.getName());
        });
        LOG.debug("Available UIs: [{}]", sb.toString());

        //last resort
        return new JPanel();
    }

    public void updateAlgorithm() {
        updateUI(getAlgorithm());
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
        Props p;
        if (optPanel != null) {
            p = optPanel.getParams();
        } else {
            p = new Props();
        }
        if (confPanel != null) {
            p.merge(confPanel.getParams());
        }

        return p;
    }

    @Override
    public void clusteringChanged(Clustering clust) {
        algBox.setEnabled(true);
        btnOptions.setEnabled(true);
        validationBox.setEnabled(true);
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //not much to do
    }

    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        //TODO: disable controls?
        algBox.setEnabled(false);
        btnOptions.setEnabled(false);
        validationBox.setEnabled(false);
    }

    public void fireBatchStarted(Dataset<? extends Instance> dataset, Props param) {
        for (ControlListener listener : controlListeners.getListeners(ControlListener.class)) {
            listener.batchStarted(dataset, param);
        }
    }

}
