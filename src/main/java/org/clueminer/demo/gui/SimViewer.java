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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedSet;
import javax.swing.JPanel;
import static org.clueminer.chameleon.Chameleon.BISECTION;
import static org.clueminer.chameleon.Chameleon.MERGER;
import static org.clueminer.chameleon.Chameleon.OBJECTIVE_1;
import static org.clueminer.chameleon.Chameleon.OBJECTIVE_2;
import static org.clueminer.chameleon.Chameleon.SIM_MEASURE;
import org.clueminer.chameleon.GraphCluster;
import org.clueminer.chameleon.PairMerger;
import org.clueminer.chameleon.mo.PairMergerMO;
import org.clueminer.chameleon.similarity.BBK1;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.Clustering;
import org.clueminer.clustering.api.EvaluationTable;
import org.clueminer.clustering.api.HierarchicalResult;
import org.clueminer.clustering.api.MergeEvaluation;
import org.clueminer.clustering.api.dendrogram.ColorScheme;
import org.clueminer.clustering.api.factory.Clusterings;
import org.clueminer.clustering.api.factory.MergeEvaluationFactory;
import org.clueminer.clustering.gui.colors.ColorSchemeImpl;
import org.clueminer.dataset.api.DataProvider;
import org.clueminer.dataset.api.Dataset;
import org.clueminer.dataset.api.Instance;
import static org.clueminer.demo.gui.AbstractClusteringViewer.RP;
import org.clueminer.dendrogram.DataProviderMap;
import org.clueminer.eval.utils.HashEvaluationTable;
import org.clueminer.graph.GraphBuilder.KNNGraphBuilder;
import org.clueminer.graph.adjacencyList.AdjListGraph;
import org.clueminer.graph.api.Graph;
import org.clueminer.graph.api.Node;
import org.clueminer.partitioning.api.Bisection;
import org.clueminer.partitioning.api.BisectionFactory;
import org.clueminer.partitioning.api.Merger;
import org.clueminer.partitioning.api.MergerFactory;
import org.clueminer.partitioning.api.Partitioning;
import org.clueminer.partitioning.impl.FiducciaMattheyses;
import org.clueminer.partitioning.impl.RecursiveBisection;
import org.clueminer.scatter.ScatterPlot;
import org.clueminer.utils.PairValue;
import org.clueminer.utils.Props;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author deric
 * @param <E>
 * @param <C>
 */
public class SimViewer<E extends Instance, C extends Cluster<E>>
        extends AbstractClusteringViewer<E, C> implements TaskListener, DatasetViewer<E, C> {

    private static final long serialVersionUID = -8355392013651815767L;

    private ScatterPlot viewer;
    private Scatter3DPlot viewer3d;
    protected Dimension reqSize = new Dimension(0, 0);

    private static final Color DRAWING_RECT_COLOR = new Color(200, 200, 255);
    private Rectangle rect = null;
    private boolean drawing = false;
    private Point mousePress = null;
    private boolean mode2d = true;
    private KNNGraphBuilder<E> knn;
    private Graph<E> graph;
    private Props pref;
    private boolean hasData = false;
    private boolean hasViewer = false;
    private PriorityQueue<PairValue<GraphCluster<E>>> pq;
    private ColorScheme colorScheme;
    private double min, max, mid;
    public SimViewer(Map<String, Dataset<? extends Instance>> data) {
        this(new DataProviderMap(data));
    }

    public SimViewer(DataProvider provider) {
        super(provider);
    }

    @Override
    protected void initComponets() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();
        knn = new KNNGraphBuilder();
        graph = new AdjListGraph();
        pref = new Props();

        //panel = new JLayeredPane();
        viewer = new ScatterPlot();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weightx = c.weighty = 1.0; //ratio for filling the frame space
        gbl.setConstraints((Component) viewer, c);
        this.add((Component) viewer, c);
        setVisible(true);
        colorScheme = new ColorSchemeImpl(Color.red, Color.BLACK, Color.GREEN);
    }

    public void setClustering(Clustering clusters) {
        if (mode2d) {
            System.out.println("clusters: " + clusters.size());
            viewer.setClustering(clusters);
        } else {
            viewer3d.setClustering(clusters);
        }

        this.clust = clusters;
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (mode2d) {
            viewer.setSize(w, h);
        } else {
            viewer3d.setSize(w, h);
        }
    }

    /**
     * Instead of running real clustering we'll display dataset with class
     * labels
     *
     * @param params
     */
    @Override
    public void execute(final Props params) {

        task = RP.post(new Runnable() {

            @Override
            public void run() {
                fireClusteringStarted(dataset, params);
                clust = goldenClustering(dataset);

            }

        });
        task.addTaskListener(this);
    }

    public Clustering goldenClustering(Dataset<? extends Instance> dataset) {
        SortedSet set = dataset.getClasses();
        Clustering golden = Clusterings.newList();

        EvaluationTable evalTable = new HashEvaluationTable(golden, dataset);
        golden.lookupAdd(evalTable);
        HashMap<Object, Integer> map = new HashMap<>(set.size());
        Object obj;
        Iterator it = set.iterator();
        int i = 0;
        Cluster c;
        cg.reset();
        int avgSize = (int) Math.sqrt(dataset.size());
        while (it.hasNext()) {
            obj = it.next();
            c = golden.createCluster(i, avgSize, obj.toString());
            c.setAttributes(dataset.getAttributes());
            c.setColor(cg.next());
            map.put(obj, i++);
        }

        int assign;

        Object klass;
        for (Instance inst : dataset) {
            if (map.containsKey(inst.classValue())) {
                assign = map.get(inst.classValue());
                c = golden.get(assign);
            } else {
                c = golden.createCluster(i);
                c.setAttributes(dataset.getAttributes());
                klass = inst.classValue();
                map.put(klass, i++);
            }
            c.add(inst);
        }
        golden.lookupAdd(dataset);

        return golden;
    }

    @Override
    public void taskFinished(Task task) {
        if (clust != null && clust.size() > 0 && clust.instancesCount() > 0) {
            setClustering(clust);
            fireClusteringChanged(clust);

            validate();
            revalidate();
            repaint();
        } else {
            System.err.println("invalid clustering");
        }
    }

    /**
     * Triggered when dataset was changed
     *
     * @param dataset
     * @param params
     */
    @Override
    public void clusteringStarted(Dataset<E> dataset, Props params) {
        //
    }

    @Override
    public void clusteringChanged(Clustering<E, C> clust) {
        if (clust != null) {
            setClustering(clust);
        }
    }

    @Override
    public void resultUpdate(HierarchicalResult hclust) {
        //
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        render((Graphics2D) g);
    }

    public void render(Graphics2D g2) {
        if (drawing) {
            g2.setColor(DRAWING_RECT_COLOR);
            g2.draw(rect);
        }

        //image = getScreenShot();
        //Graphics2D g2 = bufferedImage.createGraphics();
        //g2.drawImage(image, WIDTH, 0, this);
        Stroke stroke = new BasicStroke(2);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.50f));
        g2.setStroke(stroke);
        g2.setColor(new Color(160, 160, 160, 128));
        g2.setPaint(Color.LIGHT_GRAY);

        //draw selection
        g2.setColor(Color.RED);

        if (dataset != null && hasData) {
            Point2D source, target;

            Iterator<PairValue<GraphCluster<E>>> iter = pq.iterator();
            PairValue<GraphCluster<E>> elem;
            while (iter.hasNext()) {
                elem = iter.next();
                source = translate(elem.A.get(0));
                target = translate(elem.B.get(0));
                //drawCircle(g2, translate((E) other.getInstance()), stroke, 4);
                drawLine(g2, source, target, elem.getValue());
            }
        }

        //Area fill = new Area(new Rectangle(new Point(0, 0), getSize()));
        //g2.fill(fill);
        //g2.draw(selection);
        g2.dispose();
    }

    private void drawLine(Graphics2D g, Point2D source, Point2D target, double value) {
        if (value > 0) {
            g.setColor(colorScheme.getColor(value, min, mid, max));
            //System.out.println("val: " + value + ", " + colorScheme.getColor(value, min, mid, max));
            g.draw(new Line2D.Double(source.getX(), source.getY(), target.getX(), target.getY()));
        }
    }

    public void drawCircle(Graphics2D g, Point2D point, Stroke stroke, int markerSize) {
        g.setStroke(stroke);
        double halfSize = (double) markerSize / 2;
        Shape circle = new Ellipse2D.Double(point.getX() - halfSize, point.getY() - halfSize, markerSize, markerSize);
        g.fill(circle);

    }

    private Point2D translate(E inst) {
        return viewer.posOnCanvas(inst.get(0), inst.get(1));
    }

    @Override
    public void datasetChanged(Dataset<E> dataset) {
        //eventuall switch to 3D
        if (dataset.attributeCount() == 3) {
            mode2d = false;
            if (viewer3d == null) {
                viewer3d = new Scatter3DPlot();
            }
            setViewer(viewer3d);
        } else {
            if (!mode2d) {
                setViewer(viewer);
            }
            mode2d = true;
        }
        hasData = false;
        int datasetK = determineK(dataset);
        System.out.println("computing knn(" + datasetK + ")");
        graph = new AdjListGraph();
        graph = knn.getNeighborGraph(dataset, graph, datasetK);

        //bisection = pref.get(BISECTION, "Kernighan-Lin");
        String bisection = pref.get(BISECTION, "Fiduccia-Mattheyses");
        Bisection bisectionAlg = BisectionFactory.getInstance().getProvider(bisection);
        if (bisectionAlg instanceof FiducciaMattheyses) {
            FiducciaMattheyses fm = (FiducciaMattheyses) bisectionAlg;
            fm.setIterationLimit(pref.getInt(FiducciaMattheyses.ITERATIONS, 20));
        }

        //String partitioning = pref.get(PARTITIONING, "Recursive bisection");
        Partitioning partitioningAlg = new RecursiveBisection(bisectionAlg);
        partitioningAlg.setBisection(bisectionAlg);
        int maxPartitionSize = determineMaxPartitionSize(dataset);
        ArrayList<LinkedList<Node>> partitioningResult = partitioningAlg.partition(maxPartitionSize, graph, pref);

        String merger = pref.get(MERGER, "pair merger");
        Merger m = MergerFactory.getInstance().getProvider(merger);

        List<E> noise = null;

        m.initialize(partitioningResult, graph, bisectionAlg, pref, noise);

        MergeEvaluationFactory mef = MergeEvaluationFactory.getInstance();
        if (m instanceof PairMerger) {
            String similarityMeasure = pref.get(SIM_MEASURE, BBK1.name);
            MergeEvaluation me = mef.getProvider(similarityMeasure);
            ((PairMerger) m).setMergeEvaluation(me);
        } else if (m instanceof PairMergerMO) {
            PairMergerMO mo = (PairMergerMO) m;
            mo.clearObjectives();
            mo.addObjective(mef.getProvider(pref.get(OBJECTIVE_1)));
            mo.addObjective(mef.getProvider(pref.get(OBJECTIVE_2)));
        }

        pq = m.getQueue(pref);
        PairValue<GraphCluster<E>> head = pq.peek();
        max = head.getValue();
        mid = head.getValue() / 2.0;
        min = head.getValue() / 10.0;

        System.out.println("head: " + head.getValue());
        System.out.println("queue: " + pq.size());
        hasData = true;
    }

    private int determineK(Dataset<E> dataset) {
        if (dataset.size() < 500) {
            return (int) (Math.log(dataset.size()) / Math.log(2));
        } else {
            return (int) (Math.log(dataset.size()) / Math.log(2)) * 2;
        }
    }

    private int determineMaxPartitionSize(Dataset<E> dataset) {
        if (dataset.size() < 500) {
            return 5;
        } else if ((dataset.size() < 2000)) {
            return dataset.size() / 100;
        } else {
            return dataset.size() / 200;
        }
    }

    private void setViewer(JPanel view) {
        this.hasViewer = false;
        this.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        GridBagLayout gbl = (GridBagLayout) this.getLayout();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.weightx = c.weighty = 1.0; //ratio for filling the frame space
        gbl.setConstraints((Component) viewer, c);
        this.add((Component) view, c);
        this.hasViewer = true;
    }

    public ScatterPlot getViewer() {
        return viewer;
    }

    @Override
    public void assignLabelToSelection(String label) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean hasViewer() {
        return (mode2d && viewer != null) || (!mode2d && viewer3d != null);
    }

}
