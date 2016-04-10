package solarcar.util.graphs;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JPanel;

public class BarGraphPanel extends JPanel implements Runnable, HierarchyListener {

    private double[] pts;
    private Thread thread;
    private double ymax, ymin;
    private int xoffset;
    private int yoffset;
    private int numBars;
    private int updateFreq;
    private double highBound, lowBound;
    private Color[] cols;
    private static int count = 0;

    public BarGraphPanel(int numBars, double ymax, double ymin, int updateFreq, double lowBound, double highBound) {
        init(numBars, ymax, ymin, updateFreq, lowBound, highBound);
    }

    public BarGraphPanel(int numBars, double ymax, double ymin, int updateFreq, double highBound) {
        init(numBars, ymax, ymin, updateFreq, 0, highBound);
    }

    public BarGraphPanel(int numBars, double ymax, double ymin, int updateFreq) {
        init(numBars, ymax, ymin, updateFreq, 0, 0);
    }

    private void init(int numBars, double ymax, double ymin, int updateFreq, double lowBound, double highBound) {
        setLayout(null);

        cols = new Color[numBars];
        pts = new double[numBars];
        for (int i = 0; i < numBars; i++) {
            pts[i] = 0;
            cols[i] = new Color(0x65536 * i);
        }

        this.addHierarchyListener(this);
        this.ymax = ymax;
        this.ymin = ymin;
        this.numBars = numBars;
        this.updateFreq = updateFreq;
        this.highBound = highBound;
        this.lowBound = lowBound;

        xoffset = 10;
        yoffset = 20;

        thread = new Thread(this, "BarGraphPanel" + count);
        count++;
        thread.start();
    }

    public synchronized void addPoint(int bar, double value) {
        pts[bar] = value;
    }

    public void setColor(int bar, Color col) {
        cols[bar] = col;
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            //System.out.println("Component shown");
            synchronized (this) {
                notify();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = this.getSize();
        size.height = size.height - yoffset;
        size.width = size.width - xoffset;
        double yScale = size.height / (ymax - ymin);
        double tScale = size.width / ((double) numBars);

        g2.setColor(new Color(0, 0, 0));

        for (int i = 0; i < numBars; i++) {
            //g2.setColor(new Color(0x65536 * i * i));
            //g2.setColor(new Color(0x65536 * i));
            g2.setColor(cols[i]);
            if (pts[i] >= 0) {
                g2.fillRect((int) (i * tScale + xoffset), (int) ((ymax - pts[i]) * yScale), (int) (tScale), (int) (yScale * pts[i] + 1));
            } else {
                g2.fillRect((int) (i * tScale + xoffset), (int) (size.height - (yScale * (0 - ymin))), (int) (tScale), (int) (-pts[i] * yScale));
            }
        }

        // draw max line
        double x1 = 0 + xoffset;
        double y1 = size.height - (yScale * (ymax - ymin));
        double x2 = size.width + xoffset;
        double y2 = size.height - (yScale * (ymax - ymin));
        g2.setColor(new Color(255, 255, 0));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        // draw min line 
        x1 = 0 + xoffset;
        y1 = size.height;
        x2 = size.width + xoffset;
        y2 = size.height;
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        // draw high bound line 
        x1 = 0 + xoffset;
        y1 = size.height - (yScale * (highBound - ymin));
        x2 = size.width + xoffset;
        y2 = size.height - (yScale * (highBound - ymin));
        g2.setColor(new Color(255, 0, 0));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        // draw low bound line
        x1 = 0 + xoffset;
        y1 = size.height - (yScale * (lowBound - ymin));
        x2 = size.width + xoffset;
        y2 = size.height - (yScale * (lowBound - ymin));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        // draw zero line
        x1 = 0 + xoffset;
        y1 = size.height - (yScale * (0 - ymin));
        x2 = size.width + xoffset;
        y2 = size.height - (yScale * (0 - ymin));
        g2.setColor(new Color(130, 130, 255));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (!this.isShowing()) {
                    try {
                        //	System.out.println("waiting for graph to be visible");
                        wait();
                        //	System.out.println("checking if graph is visible");
                    } catch (Exception e) {
                        //
                    }
                }
            }

            /*
             * for( int i = 0; i < numBars; i++) { this.addPoint(i, 2.1 + 2.1 *
             * rand.nextDouble());
			}
             */
            // update graph
            //System.out.println("updating graph\t");
            this.repaint();
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
            }
        }
    }
}