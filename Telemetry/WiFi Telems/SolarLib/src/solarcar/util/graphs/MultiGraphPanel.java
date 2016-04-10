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

public class MultiGraphPanel extends JPanel implements Runnable, HierarchyListener {

    private SolarPoint[][] pts;
    private int timeLength;
    private int curPt[];
    private int writePt[];
    private int updateFreq;
    private Thread thread;
    private JLabel endLabel;
    private double ymax, ymin;
    private int xoffset;
    private int yoffset;
    private int numLines;
    private Color[] cols;
    private String toplimit, bottomlimit, midlimit, axistime_min, axistime_max;
    private int labelx_max, labely_max, labely_min, labelx_min, labelx_mid, labely_mid, label_offset;
    private int labeltime_min, labeltime_max, timelabel_offset;
    private int num_lines;
    private static int count = 0;
	private DecimalFormat df;

        public MultiGraphPanel(int timeLength, int messageFrequency, int updateFrequency,
            double ymax, double ymin, int numLines) {
        setLayout(null);

        cols = new Color[numLines];
        curPt = new int[numLines];
        writePt = new int[numLines];
        pts = new SolarPoint[numLines][(timeLength + 2) * messageFrequency];
        for (int j = 0; j < numLines; j++) {
            for (int i = 0; i < pts[j].length; i++) {
                pts[j][i] = new SolarPoint();
            }
            curPt[j] = 0;
            writePt[j] = 0;
            cols[j] = new Color(0x65536 * j);
        }

        this.timeLength = timeLength;

        this.updateFreq = updateFrequency;

        this.ymax = ymax;
        this.ymin = ymin;

        xoffset = 10;
        yoffset = 20;
        label_offset = 9;
        timelabel_offset = 12;

        this.numLines = numLines;

//		endLabel = new JLabel(Integer.toString(timeLength));
//		this.add(endLabel);		
		df = new DecimalFormat("0.##");
		this.toplimit = df.format(ymax);
		this.bottomlimit = df.format(ymin);
		this.midlimit = df.format((ymax+ymin) / 2);
		
        this.axistime_min = Integer.toString(0);
        this.axistime_max = ((int) (this.timeLength / 3600)) + ":" + ((((int) ((this.timeLength % 3600) / 60)) < 10) ? "0" : "") + ((int) ((this.timeLength % 3600) / 60)) + ":" + (((int) (this.timeLength % 60) < 10) ? "0" : "") + (this.timeLength % 60);
        num_lines = 4;
    }
	
	public synchronized void start() {
		this.addHierarchyListener(this);

        thread = new Thread(this, "MultiGraphPanel" + count);
        count++;
        
        thread.start();
	}

    public synchronized void addPoint(int line, double value) {
        //System.out.println("New point: ");
        pts[line][curPt[line]].time = System.currentTimeMillis();
        pts[line][curPt[line]].value = value - ymin;

        curPt[line] = (curPt[line] - 1 + pts[line].length) % pts[line].length;
    }
    public synchronized void addPoint(int line, double value, long time)
    {
        pts[line][curPt[line]].time = time;
        pts[line][curPt[line]].value = value - ymin;

        curPt[line] = (curPt[line] - 1 + pts[line].length) % pts[line].length;
    }
    public synchronized void addPointFromWrite(int line, double value, long time)
    {
        pts[line][writePt[line]].time = time;
        pts[line][writePt[line]].value = value - ymin;

        if(writePt[line]==curPt[line])
        {
            curPt[line] = (curPt[line] - 1 + pts[line].length) % pts[line].length;
        }
        writePt[line] = (writePt[line] - 1 + pts[line].length) % pts[line].length;
    }
    public synchronized void resetWriteHead(int line)
    {
        writePt[line]=0;
    }
    public synchronized void dispWriteSection(int line)
    {
        curPt[line]=writePt[line];
    }

    
    public synchronized void insertNullPt(int line)
    {
        pts[line][curPt[line]].time = -1;
        pts[line][curPt[line]].value = -1;
        curPt[line] = (curPt[line] - 1 + pts[line].length) % pts[line].length;
    }
    
    public int getLineLength(int line)
    {
        return pts[line].length;
    }
    public int getTimeLength()
    {
        return timeLength;
    }
    public double getYMin()
    {
        return ymin;
    }

    public void setColor(int line, Color col) {
        cols[line] = col;
    }

    public void setNumLines(int num) {
        this.num_lines = num;
    }

    public void resetLine(int line){
        for (int i = 0; i < pts[line].length; ++i){
            //pts[line][i].value = 0;
            pts[line][i] = new SolarPoint();
        }
        
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
        //g2.setColor(new Cgrpaholor(0,0,255));
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


        for (int j = 0; j < numLines; j++) {
            g2.setColor(new Color(0x65535 * j));
            g2.setColor(cols[j]);
            x1 = size.width - ((tScale) * (time - pts[j][(curPt[j] + 1) % pts[j].length].time)) + xoffset;
            y1 = size.height - (yScale * pts[j][(curPt[j] + 1) % pts[j].length].value);

            //System.out.println("drawing\t" + time + "\t" + size.width + "\t" + tScale+ "\t" + time);

            for (int i = (curPt[j] + 1) % pts[j].length; i != curPt[j] - 2; i = (i + 1) % pts[j].length) {
                //System.out.println(pts[i].time +"\t" + pts[i].value + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
                if (pts[j][i].value == -1) {
                    break;
                }
                x2 = size.width - ((tScale) * (time - pts[j][(i + 1) % pts[j].length].time)) + xoffset;
                y2 = size.height - (yScale * pts[j][(i + 1) % pts[j].length].value);
                if (x2 < xoffset) {
                    break;
                }

                g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                //System.out.println(pts[i].time +"\t" + pts[i].value + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
                x1 = x2;
                y1 = y2;
            }
        }
        //Axis Labels for graphs,yaxis
        //	this.toplimit = Integer.toString((int)ymax);
        //	this.bottomlimit = Integer.toString((int)ymin);
        //	this.midlimit = Integer.toString((int)(ymax-Math.abs(ymin))/2);
        //	this.axistime_min = Integer.toString(0);
        //	this.axistime_max = Integer.toString((int)this.timeLength);
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
