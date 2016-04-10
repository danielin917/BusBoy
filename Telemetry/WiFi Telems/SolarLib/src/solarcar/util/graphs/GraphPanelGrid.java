package solarcar.util.graphs;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import solarcar.util.SolarPoint;

public class GraphPanelGrid extends JPanel implements Runnable, HierarchyListener {

    private SolarPoint[] pts;
    private int timeLength;
    private int curPt;
    private int updateFreq;
    private Thread thread;
    private JLabel endLabel;
    private double ymax, ymin;
    private int xoffset;
    private int yoffset;
    private Color col;
    private String toplimit, bottomlimit, midlimit, axistime_min, axistime_max;
    private int labelx_max, labely_max, labely_min, labelx_min, labelx_mid, labely_mid, label_offset;
    private int timelabel_offset;
    private int num_lines;
    private static int count = 0;
	private DecimalFormat df;

    public GraphPanelGrid(int timeLength, int messageFrequency, int updateFrequency,
            double ymax, double ymin) {

        // set the layout to null
        setLayout(null);

        pts = new SolarPoint[(timeLength + 2) * messageFrequency];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new SolarPoint();
        }

        this.timeLength = timeLength;
        curPt = 0;

        this.updateFreq = updateFrequency;

        this.ymax = ymax;
        this.ymin = ymin;

        xoffset = 10;
        yoffset = 20;
        label_offset = 9;
        timelabel_offset = 12;

        endLabel = new JLabel(Integer.toString(timeLength));
        this.add(endLabel);

		df = new DecimalFormat("0.##");
		this.toplimit = df.format(ymax);
		this.bottomlimit = df.format(ymin);
		this.midlimit = df.format((ymax+ymin) / 2);

        this.axistime_min = Integer.toString(0);

        this.axistime_max = ((int) (this.timeLength / 3600)) + ":" + ((((int) ((this.timeLength % 3600) / 60)) < 10) ? "0" : "") + ((int) ((this.timeLength % 3600) / 60)) + ":" + (((int) (this.timeLength % 60) < 10) ? "0" : "") + (this.timeLength % 60);

        this.col = Color.BLACK;

        this.num_lines = 4;
    }
	
	public synchronized void start() {
		this.addHierarchyListener(this);
		thread = new Thread(this, "GraphPanelGrid" + count);
        count++;
        thread.start();
	}

    public synchronized void addPoint(double value) {
        //System.out.println("New point: ");
        pts[curPt].time = System.currentTimeMillis();
        pts[curPt].value = value - ymin;
        //	System.out.println("New point: " + pts[curPt].time + "\t" + pts[curPt].value);
        curPt = (curPt - 1 + pts.length) % pts.length;
    }

    public void setColor(Color col) {
        this.col = col;
    }

    public void setNumLines(int num) {
        this.num_lines = num;
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
        double tScale = size.width / ((double) timeLength * 1000);
        long time = System.currentTimeMillis();

        double x1 = 0 + xoffset;
        double y1 = size.height - (yScale * (ymax - ymin));
        double x2 = size.width + xoffset;
        double y2 = size.height - (yScale * (ymax - ymin));
        g2.setColor(new Color(255, 255, 0));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        x1 = 0 + xoffset;
        y1 = size.height;// - (yScale * (0-ymin));
        x2 = size.width + xoffset;
        y2 = size.height;// - (yScale * (0-ymin));
        //g2.setColor(new Color(0,0,255));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g2.setColor(new Color(0, 255, 0));
        int line_percent = 1;
        for (int i = 0; i < num_lines; i++) {
            g2.drawLine((int) x1, (int) (y1 * line_percent * (1.0 / num_lines)), (int) x2, (int) (y1 * line_percent * (1.0 / num_lines)));
            line_percent = line_percent + 1;
        }

        x1 = 0 + xoffset;
        y1 = size.height - (yScale * (0 - ymin));
        x2 = size.width + xoffset;
        y2 = size.height - (yScale * (0 - ymin));
        g2.setColor(new Color(130, 130, 255));
        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g2.setColor(new Color(0, 0, 0));


        x1 = size.width - ((tScale) * (time - pts[(curPt + 1) % pts.length].time)) + xoffset;
        y1 = size.height - (yScale * pts[(curPt + 1) % pts.length].value);

        //System.out.println("drawing\t" + time + "\t" + size.width + "\t" + tScale+ "\t" + time);

        g2.setColor(col);

        for (int i = (curPt + 1) % pts.length; i != curPt - 2; i = (i + 1) % pts.length) {
            //System.out.println(pts[i].time +"\t" + pts[i].value + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
            if (pts[i].value == -1) {
                break;
            }
            x2 = size.width - ((tScale) * (time - pts[(i + 1) % pts.length].time)) + xoffset;
            y2 = size.height - (yScale * pts[(i + 1) % pts.length].value);
            if (x2 < xoffset) {
                break;
            }

            g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            //System.out.println(pts[i].time +"\t" + pts[i].value + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
            x1 = x2;
            y1 = y2;
        }
        //Axis Labels for graphs,yaxis
//		this.toplimit = Integer.toString((int)ymax);
//		this.bottomlimit = Integer.toString((int)ymin);
//		this.midlimit = Integer.toString((int)(ymax-Math.abs(ymin))/2);
//		this.axistime_min = Integer.toString(0);
//		this.axistime_max = Integer.toString((int)this.timeLength);
        //Dimension = 
        labelx_max = 0;
        labelx_mid = 0;
        labelx_min = 0;
        labely_max = label_offset; //manual offset
        labely_mid = size.height / 2;//+label_offset;
        labely_min = size.height;
        g2.setColor(Color.BLACK);
        //labeltime_max = this.timeLength; //max history
        //labeltime_min = 0;
        g2.drawString(toplimit, labelx_max, labely_max);
        g2.drawString(bottomlimit, labelx_min, labely_min);
        g2.drawString(midlimit, labelx_mid, labely_mid);




        g2.drawString(axistime_min, size.width, size.height + timelabel_offset);
        g2.drawString(axistime_max, 15, size.height + timelabel_offset);
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
            //System.out.println("updating graph\t" +  dummy.isShowing());
            this.repaint();
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
            }
        }
    }
}
