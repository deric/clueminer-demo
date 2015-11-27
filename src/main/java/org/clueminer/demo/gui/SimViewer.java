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
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedSet;
import javax.swing.JPanel;
import org.clueminer.chameleon.Chameleon;
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
import org.clueminer.graph.adjacencyList.AdjListGraph;
import org.clueminer.graph.api.Edge;
import org.clueminer.graph.api.Graph;
import org.clueminer.graph.api.Node;
import org.clueminer.graph.knn.KNNGraphBuilder;
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
    private int total;
    private Merger<E> merger;
    private int alpha;

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
        pref = params;
        task = RP.post(new Runnable() {

            @Override
            public void run() {
                fireClusteringStarted(dataset, params);
                //clust = goldenClustering(dataset);
                if (hasData && merger != null) {
                    clust = partitionedClustering(merger);
                }

            }

        });
        task.addTaskListener(this);
    }

    private Clustering partitionedClustering(Merger<E> m) {
        Clustering clustering = Clusterings.newList();

        cg.reset();
        total = 0;
        for (Cluster<E> cluster : m.getClusters()) {
            //  clust = clustering.createCluster();
            if (cg != null) {
                cluster.setColor(cg.next());
            }
            clustering.add(cluster);
            total += cluster.size();
        }
        System.out.println("total points: " + total);

        return clustering;
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
        revalidate();
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
        Point2D source, target;

        if (dataset != null && hasData) {
            Iterator<PairValue<GraphCluster<E>>> iter = pq.iterator();
            PairValue<GraphCluster<E>> elem;

            double edgeMin = Double.MAX_VALUE;
            double edgeMax = Double.MIN_VALUE;
            double value;

            while (iter.hasNext()) {
                elem = iter.next();
                source = translate(elem.A.getCentroid());
                //source = translate(elem.A.get(0));
                target = translate(elem.B.getCentroid());
                //target = translate(elem.B.get(0));
                //drawCircle(g2, translate((E) other.getInstance()), stroke, 4);
                value = elem.getValue();
                drawLine(g2, source, target, value);

                if (value > edgeMax) {
                    edgeMax = value;
                }
                if (value > 0 && value < edgeMin) {
                    edgeMin = value;
                }
            }

            max = edgeMax;
            mid = (edgeMax - edgeMin) / 2.0;
            min = edgeMin;
            /*System.out.println("==== partition");
             System.out.println("min = " + min);
            System.out.println("mid = " + mid);
            System.out.println("max = " + max);*/
        } else if (graph != null) {
            double edgeMin = Double.MAX_VALUE;
            double edgeMax = Double.MIN_VALUE;

            for (Edge e : graph.getEdges()) {
                source = translate((E) e.getSource().getInstance());
                //source = translate(elem.A.get(0));
                target = translate((E) e.getTarget().getInstance());
                //target = translate(elem.B.get(0));
                //drawCircle(g2, translate((E) other.getInstance()), stroke, 4);
                drawLine(g2, source, target, e.getWeight());

                if (e.getWeight() > edgeMax) {
                    edgeMax = e.getWeight();
                }
                if (e.getWeight() > 0 && e.getWeight() < edgeMin) {
                    edgeMin = e.getWeight();
                }
            }
            /*     System.out.println("=====edges ");
             max = edgeMax;
            mid = (edgeMax - edgeMin) / 2.0;
            min = edgeMin;
            System.out.println("min = " + min);
            System.out.println("mid = " + mid);
            System.out.println("max = " + max);*/
        }

        /*if (merger != null) {
         g2.setColor(Color.PINK);            for (Cluster<E> cluster : merger.getClusters()) {
                drawCircle(g2, translate((E) cluster.get(0)), stroke, 4);
            }
        }*/

        //Area fill = new Area(new Rectangle(new Point(0, 0), getSize()));
        //g2.fill(fill);
        //g2.draw(selection);
        g2.dispose();
    }

    private void drawLine(Graphics2D g, Point2D source, Point2D target, double value) {
        Color c;
        if (value > 0) {
            c = colorScheme.getColor(value, min, mid, max);
            //g.setColor(c);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
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
        computeNN(dataset, pref);
    }

    public void computeNN(Dataset<E> dataset, Props pref) {
        hasData = false;
        int datasetK = pref.getInt(Chameleon.K, determineK(dataset));
        System.out.println("dataset size: " + dataset.size());
        System.out.println("computing knn(" + datasetK + ")");
        graph = new AdjListGraph();
        graph.lookupAdd(dataset);
        graph = knn.getNeighborGraph(dataset, graph, datasetK);
        alpha = pref.getInt("alpha", 255);

        //bisection = pref.get(BISECTION, "Kernighan-Lin");
        String bisection = pref.get(BISECTION, "Fiduccia-Mattheyses");
        Bisection bisectionAlg = BisectionFactory.getInstance().getProvider(bisection);
        if (bisectionAlg instanceof FiducciaMattheyses) {
            FiducciaMattheyses fm = (FiducciaMattheyses) bisectionAlg;
            fm.setIterationLimit(pref.getInt(FiducciaMattheyses.ITERATIONS, 20));
        }
        System.out.println(pref.toString());

        if (!pref.containsKey("skip_partition")) {
            //String partitioning = pref.get(PARTITIONING, "Recursive bisection");
            Partitioning partitioningAlg = new RecursiveBisection(bisectionAlg);
            partitioningAlg.setBisection(bisectionAlg);
            int maxPartitionSize = pref.getInt(Chameleon.MAX_PARTITION, determineMaxPartitionSize(dataset));
            System.out.println("max. partition = " + maxPartitionSize);
            ArrayList<ArrayList<Node<E>>> partitioningResult = partitioningAlg.partition(maxPartitionSize, graph, pref);

            String mergerMth = pref.get(MERGER, "pair merger");
            merger = MergerFactory.getInstance().getProvider(mergerMth);

            ArrayList<E> noise = null;

            MergeEvaluationFactory mef = MergeEvaluationFactory.getInstance();
            if (merger instanceof PairMerger) {
                String similarityMeasure = pref.get(SIM_MEASURE, BBK1.name);
                MergeEvaluation me = mef.getProvider(similarityMeasure);
                ((PairMerger) merger).setMergeEvaluation(me);
            } else if (merger instanceof PairMergerMO) {
                PairMergerMO mo = (PairMergerMO) merger;
                mo.clearObjectives();
                mo.addObjective(mef.getProvider(pref.get(OBJECTIVE_1)));
                mo.addObjective(mef.getProvider(pref.get(OBJECTIVE_2)));
            }
            merger.initialize(partitioningResult, graph, bisectionAlg, pref, noise);

            /*    double edgeMin = Double.MAX_VALUE;
             double edgeMax = Double.MIN_VALUE;
             for (Edge e : graph.getEdges()) {
             if (e.getWeight() > edgeMax) {
             edgeMax = e.getWeight();
             }
             if (e.getWeight() > 0 && e.getWeight() < edgeMin) {
             edgeMin = e.getWeight();
             }
             }*/
            pq = merger.getQueue(pref);
            /*   //PairValue<GraphCluster<E>> head = pq.peek();
             max = edgeMax;
             mid = (edgeMax - edgeMin) / 2.0;
             min = edgeMin;

             System.out.println("min = " + min);
             System.out.println("mid = " + mid);
             System.out.println("max = " + max);*/

            System.out.println("queue: " + pq.size());
            hasData = true;
        } else {
            clust = goldenClustering(dataset);
        }
        validate();
        revalidate();
        repaint();
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
