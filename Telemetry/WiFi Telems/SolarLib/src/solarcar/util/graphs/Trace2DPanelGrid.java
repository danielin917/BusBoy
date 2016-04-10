package solarcar.util.graphs;


import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import solarcar.util.SolarPoint2D;

public class Trace2DPanelGrid extends JPanel implements Runnable, HierarchyListener {

    private SolarPoint2D[] pts;
    private int curPt;
    private int updateFreq;
    private Thread thread;
    private JLabel endLabel;
    private double xmax, xmin;
    private double ymax, ymin;
    private int xoffset;
    private int yoffset;
    private Color col;
    private static int count = 0;
    //Random r; 
    //private double debugX, debugY;

    public Trace2DPanelGrid(int timeLength, int messageFrequency, int updateFrequency,
            double xmax, double xmin, double ymax, double ymin) {

        // set the layout to null
        setLayout(null);

        pts = new SolarPoint2D[(timeLength + 2) * messageFrequency];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new SolarPoint2D();
        }

        curPt = 0;

        this.updateFreq = updateFrequency;
        this.ymax = ymax;
        this.ymin = ymin;
        this.xmax = xmax;
        this.xmin = xmin;

        xoffset = 10;
        yoffset = 20;

        endLabel = new JLabel(Integer.toString(timeLength));
        this.add(endLabel);

        this.col = Color.BLACK;

        //r = new Random();
        //debugX = 2.5; debugY = 30;
    }
	
	public synchronized void start() {
		this.addHierarchyListener(this);

        thread = new Thread(this, "Trace2DPanelGrid" + count);
        count++;
        thread.start();
	}

    public synchronized void addPoint(double x, double y) {
        //System.out.println("New point: ");
        pts[curPt].time = System.currentTimeMillis();
        pts[curPt].x = x - xmin;
        pts[curPt].y = y - ymin;
        //	System.out.println("New point: " + pts[curPt].time + "\t" + pts[curPt].value);
        curPt = (curPt - 1 + pts.length) % pts.length;
    }

    public void setColor(Color col) {
        this.col = col;
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
        double xScale = size.width / (xmax - xmin);
        long time = System.currentTimeMillis();

        double x1 = 0 + xoffset;
        double y1 = size.height - (yScale * (ymax - ymin));
        double x2 = size.width + xoffset;
        double y2 = size.height - (yScale * (ymax - ymin));
        g2.setColor(new Color(255, 255, 0));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        //Draw axes
        x1 = 0 + xoffset;
        y1 = size.height;// - (yScale * (0-ymin));
        x2 = size.width + xoffset;
        y2 = size.height;// - (yScale * (0-ymin));
        //g2.setColor(new Color(0,0,255));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);


        x1 = 0 + xoffset;
        y1 = size.height - (yScale * (0 - ymin));
        x2 = size.width + xoffset;
        y2 = size.height - (yScale * (0 - ymin));
        g2.setColor(new Color(130, 130, 255));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);


        x1 = (xScale * (0 - xmin));
        y1 = 0;
        x2 = (xScale * (0 - xmin));
        y2 = size.height;
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g2.setColor(new Color(0, 0, 0));

        x1 = (xScale * pts[(curPt + 1) % pts.length].x);
        y1 = size.height - (yScale * pts[(curPt + 1) % pts.length].y);

        //System.out.println("drawing\t" + time + "\t" + size.width + "\t" + tScale+ "\t" + time);

        g2.setColor(col);
        g2.setStroke(new BasicStroke(5));
        Stroke s = new BasicStroke(1);
        //System.out.println();
        for (int i = (curPt + 1) % pts.length; i != curPt - 2; i = (i + 1) % pts.length) {
            if (pts[i].time == -1) {
                break;
            }
            x2 = (xScale * pts[(i + 1) % pts.length].x);
            y2 = size.height - (yScale * pts[(i + 1) % pts.length].y);
            //if(x2 < xoffset) break;

            //System.out.println(pts[i].time + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
            g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            g2.setStroke(s);
            //System.out.println(pts[i].time +"\t" + pts[i].value + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
            x1 = x2;
            y1 = y2;
            g2.setColor(Color.GRAY);
        }
        //Axis Labels for graphs,yaxis
//		this.toplimit = Integer.toString((int)ymax);
//		this.bottomlimit = Integer.toString((int)ymin);
//		this.midlimit = Integer.toString((int)(ymax-Math.abs(ymin))/2);
//		this.axistime_min = Integer.toString(0);
//		this.axistime_max = Integer.toString((int)this.timeLength);
        //Dimension = 

        //labeltime_max = this.timeLength; //max history
        //labeltime_min = 0;
        //g2.drawString(toplimit, labelx_max, labely_max);
        //g2.drawString(bottomlimit, labelx_min, labely_min);
        //g2.drawString(midlimit, labelx_mid, labely_mid);


        //g2.drawString(axistime_min, size.width, size.height+timelabel_offset);
        //g2.drawString(axistime_max, 15, size.height+timelabel_offset);
        //g2.drawString(midlimit, labelx_mid, labely_mid);

    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (!this.isShowing()) {
                    try {
                        //System.out.println("waiting for graph to be visible");
                        wait();
                    } catch (Exception e) {
                        //
                    }
                }
            }

            // update graph
            //System.out.println("updating graph\t" +  dummy.isShowing
            //debugX+= r.nextDouble() * 0.2 - 0.1;
            //debugY+= r.nextDouble() * 3 - 1.5;
            //this.addPoint(0, 0);
            this.repaint();
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
            }
        }
    }
}
