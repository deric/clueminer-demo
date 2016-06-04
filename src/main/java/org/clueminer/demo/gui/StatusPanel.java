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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.ClusterEvaluation;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.ClusteringListener;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import org.clueminer.eval.external.NMIsqrt;
import org.clueminer.utils.Props;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class StatusPanel<E extends Instance, C extends Cluster<E>> extends JPanel implements ClusteringListener<E, C>, ControlListener {

    private static final long serialVersionUID = -2919926609337848228L;

    private JProgressBar progressBar;
    private JLabel lbStatus;
    private JButton btnStop;
    private long startTime;
    private final DatasetViewer plot;
    private ClusterEvaluation evaluator;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private Clustering<E, C> clustering;
    private double scoreSum;
    private int repeatCnt;

    public StatusPanel(DatasetViewer plot) {
        this.plot = plot;
        evaluator = new NMIsqrt();
        initComponents();
    }

    private void initComponents() {
        plot.addClusteringListener(this);
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        lbStatus = new JLabel("");
        add(lbStatus);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        add(progressBar);
        btnStop = new JButton("Stop");
        btnStop.setEnabled(false);
        add(btnStop);
        btnStop.setVisible(false);
        btnStop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnStop.setEnabled(false);
                plot.abort();
                btnStop.setEnabled(true);
            }
        });
    }

    @Override
    public void clusteringChanged(Clustering clust) {

        long time = System.currentTimeMillis() - startTime;
        if (clust != null) {
            repeatCnt += 1;
            StringBuilder sb = new StringBuilder();
            sb.append("Clustering took ").append(TimeUnit.MILLISECONDS.convert(time, TimeUnit.MILLISECONDS))
                    .append(" ms");
            Dataset<? extends Instance> dataset = clust.getLookup().lookup(Dataset.class);
            if (dataset != null) {
                sb.append(", dataset size: ").append(dataset.size()).append(" x ")
                        .append(dataset.attributeCount());
                sb.append(", classes: ").append(dataset.getClasses().size());
            }
            sb.append(", total clusters: ").append(clust.size());
            double score = evaluator.score(clust);
            System.out.println(evaluator.getName() + ": " + score + " [" + repeatCnt + "]");
            scoreSum += score;
            sb.append(", ").append(evaluator.getName()).append(": ").append(decimalFormat.format(score));
            double avg = scoreSum / (double) repeatCnt;
            System.out.println("avg = " + avg);
            sb.append(", ").append("avg").append(": ").append(decimalFormat.format(avg));

            lbStatus.setText(sb.toString());
        } else {
            lbStatus.setText("Clustering stopped.");
        }

        progressBar.setVisible(false);
        btnStop.setEnabled(false);
        btnStop.setVisible(false);
        //
        this.clustering = clust;
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //
    }

    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        lbStatus.setText("Clustering " + dataset.getName() + " with " + params.get("algorithm") + " ...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        btnStop.setEnabled(true);
        btnStop.setVisible(true);
        startTime = System.currentTimeMillis();
        //
    }

    public void setEvaluator(ClusterEvaluation evaluator) {
        this.evaluator = evaluator;
        clusteringChanged(clustering);
    }

    @Override
    public void batchStarted(Dataset<? extends Instance> dataset, Props params) {
        repeatCnt = 0;
        scoreSum = 0.0;
    }

}
