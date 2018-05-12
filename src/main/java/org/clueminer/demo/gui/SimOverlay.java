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
package org.clueminer.demo.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import javax.swing.JPanel;
import org.clueminer.chameleon.GraphCluster;
import org.clueminer.clustering.api.Cluster;
import org.clueminer.clustering.api.dendrogram.ColorScheme;
import org.clueminer.clustering.gui.colors.ColorSchemeImpl;
import org.clueminer.dataset.api.Instance;
import org.clueminer.graph.api.Edge;
import org.clueminer.graph.api.EdgeType;
import org.clueminer.gui.BPanel;
import org.clueminer.utils.PairValue;

/**
 * Graph edges visualization
 *
 * @author deric
 */
public class SimOverlay<E extends Instance, C extends Cluster<E>> extends JPanel {

    private final Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
    private final Stroke basic = new BasicStroke(3);

    private static final Color DRAWING_RECT_COLOR = new Color(200, 200, 255);
    private Rectangle rect = null;
    private boolean drawing = false;
    private Point mousePress = null;
    private SimViewer<E, Cluster<E>> parent;
    private ColorScheme colorScheme;
    private double min, max, mid;

    public SimOverlay(SimViewer viewer) {
        setLayout(null);
        this.parent = viewer;
        colorScheme = new ColorSchemeImpl(Color.red, Color.BLACK, Color.GREEN);
    }

    public void paint(Graphics g) {
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
        //g2.setPaint(Color.PINK);

        //draw selection
        g2.setColor(Color.RED);
        Point2D source, target;

        if (parent.getDataset() != null && parent.hasData()) {
            Iterator<PairValue<GraphCluster<E>>> iter = parent.pq.iterator();
            PairValue<GraphCluster<E>> elem;

            double edgeMin = Double.MAX_VALUE;
            double edgeMax = Double.MIN_VALUE;
            double value;

            while (iter.hasNext()) {
                elem = iter.next();
                source = parent.translate(elem.A.getCentroid());
                //source = translate(elem.A.get(0));
                target = parent.translate(elem.B.getCentroid());
                //target = translate(elem.B.get(0));
                //drawCircle(g2, translate((E) other.getInstance()), stroke, 4);
                value = elem.getValue();
                drawLine(g2, source, target, value, EdgeType.NONE);

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
            /* System.out.println("==== partition");
             * System.out.println("min = " + min);
             * System.out.println("mid = " + mid);
             * System.out.println("max = " + max); */
        } else if (parent.getGraph() != null) {
            double edgeMin = Double.MAX_VALUE;
            double edgeMax = Double.MIN_VALUE;

            for (Edge e : parent.getGraph().getEdges()) {
                source = parent.translate((E) e.getSource().getInstance());
                //source = translate(elem.A.get(0));
                target = parent.translate((E) e.getTarget().getInstance());
                //target = translate(elem.B.get(0));
                //drawCircle(g2, translate((E) other.getInstance()), stroke, 4);
                drawLine(g2, source, target, e.getWeight(), e.getDirection());

                if (e.getWeight() > edgeMax) {
                    edgeMax = e.getWeight();
                }
                if (e.getWeight() > 0 && e.getWeight() < edgeMin) {
                    edgeMin = e.getWeight();
                }
            }
            /* System.out.println("=====edges ");
             * max = edgeMax;
             * mid = (edgeMax - edgeMin) / 2.0;
             * min = edgeMin;
             * System.out.println("min = " + min);
             * System.out.println("mid = " + mid);
             * System.out.println("max = " + max); */
        }

        if (parent.getMerger() != null) {
            g2.setColor(Color.ORANGE);
            for (Cluster<E> cluster : parent.getMerger().getClusters()) {
                if (cluster.size() == 1) {
                    //drawCircle(g2, translate((E) cluster.get(0)), stroke, 4);
                    drawCross(g2, parent.translate(cluster.get(0)), stroke, 15, Color.BLACK);
                }
            }
        }
        //Area fill = new Area(new Rectangle(new Point(0, 0), getSize()));
        //g2.fill(fill);
        //g2.draw(selection);
        g2.dispose();
    }

    private void drawCross(Graphics2D g, Point2D point, Stroke stroke, int diameter, Color color) {
        g.setColor(color);
        g.draw(new Line2D.Double(point.getX() - diameter, point.getY() + diameter, point.getX() + diameter, point.getY() - diameter));
        g.draw(new Line2D.Double(point.getX() - diameter, point.getY() - diameter, point.getX() + diameter, point.getY() + diameter));
        drawCircle(g, point, stroke, 6);

    }

    private void drawLine(Graphics2D g, Point2D source, Point2D target, double value, EdgeType direction) {
        Color c;
        if (value > 0) {
            c = colorScheme.getColor(value, min, mid, max);
            //g.setColor(c);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), parent.getAlpha()));
            //System.out.println("val: " + value + ", " + colorScheme.getColor(value, min, mid, max));
            if (parent.getPref().getBoolean("unidirect_dashed", false)) {
                if (direction == EdgeType.BOTH) {
                    g.setStroke(basic);
                } else {
                    g.setStroke(dashed);
                }
            }
            g.draw(new Line2D.Double(source.getX(), source.getY(), target.getX(), target.getY()));
        }
    }

    public void drawCircle(Graphics2D g, Point2D point, Stroke stroke, int markerSize) {
        g.setStroke(stroke);
        double halfSize = (double) markerSize / 2;
        Shape circle = new Ellipse2D.Double(point.getX() - halfSize, point.getY() - halfSize, markerSize, markerSize);
        g.fill(circle);

    }

    public void sizeUpdated(Dimension size) {
        //this.realSize = size;
    }

    public boolean hasData() {
        return parent.hasData();
    }

    public void recalculate() {

    }

    public boolean isAntiAliasing() {
        return true;
    }

}
