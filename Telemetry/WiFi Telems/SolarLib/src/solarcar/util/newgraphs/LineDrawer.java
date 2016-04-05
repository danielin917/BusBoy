/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.newgraphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * A class which handles drawing lines on graphs.
 *
 * @author aaresh
 */
public class LineDrawer {

	/*
	 * Initiated at construction
	 */
	Color color;
	int curPt;

	/*
	 * Initiated in setGraphParent
	 */
	Graph parent = null;
	DataPoint points[];
	

	/**
	 * Creates a new <code>LineDrawer</code> using the default color 
	 * (black).
	 */
	public LineDrawer() {
		this.initLine(Color.black);
	}

	/**
	 * Creates a new <code>LineDrawer<code> using the specified color.
	 *
	 * @param col	The color to draw the line 
	 */
	public LineDrawer(Color col) {
		this.initLine(col);
	}

	/**
	 * Adds a new point to plot.
	 *
	 * @param point		The new point to plot
	 */
	public synchronized void addPoint(double point) {
		// TODO: test;
		if(this.parent != null) {
			this.points[this.curPt].AddPoint(this.parent.getPixelY(point));
		}
	}
	
	/**
	 * Sets the color to draw the line. 
	 * @param col The color to draw the line. 
	 */
	public synchronized void setColor(Color col) {
		// TODO: test;
		this.color = col;
	}

	/**
	 * Draws the plot on the specified
	 * <code>Graphics2D</code> object. This should only be called after this
	 * <code>LineDrawer</code> has been told the panel size or parent graph.
	 *
	 * @param g2d	The graphics object to draw the plot on
	 */
	protected synchronized void drawLine(Graphics2D g2d) {
		// TODO: test
		this.drawAverageLine(g2d);
	}
	
	/**
	 * Draws the line of the average of each point on the specified
	 * <code>Graphics2D</code> object. This should really only be called
	 * by <code>drawLine</code>. 
	 * @param g2d	The graphics object to draw the lines on
	 */
	private synchronized void drawAverageLine(Graphics2D g2d) {
		int x1, x2, pt;
		DataPoint y1, y2;

		pt = this.curPt;//this.nextPtIndex();
		x1 = 0;
		y1 = this.points[pt];
		x2 = 0;
		pt = this.nextPtIndex(pt);
		
		g2d.setColor(this.color);
		while(x2 < this.points.length) {
			x2 = x1 + 1;
			y2 = this.points[pt];
			
			g2d.drawLine(x1, y1.getMean(), x2, y2.getMean());
			
			x1 = x2;
			y1 = y2;
			pt = this.nextPtIndex(pt);
		}
	}

	/**
	 * The graph object needs to tell the LineDrawer when the time for the
	 * current pixel has expired, and it is time to move on to the next
	 * pixel. 
	 */
	protected synchronized void advancePixel() {
		// TODO: test
		this.curPt = nextPtIndex();
		this.points[this.curPt].reset();
	}
	
	/**
	 * Tells the line it's parent graph in order to get it's properties. 
	 * This <b>must</b> be called before <code>drawLine</code>
	 * is called. If <code>addPoint</code> is called before this is called, 
	 * the data given to addPoint will be ignored until this is called. In order
	 * to optimize memory usage, we need to know how many points we need to 
	 * store before we allocate the memory to store it. We don't know how 
	 * many points we need to store until we know the size of the graph. 
	 * 
	 * @param g		The parent graph.
	 */
	protected synchronized void setGraphParent(Graph g) {
		// TODO: create & populate array
		this.points = new DataPoint[g.pastPixels];
		for(int i = 0; i < this.points.length; i++) {
			this.points[i] = new DataPoint();
			this.points[i].setDefaultMean(g.getPixelY(0));
		}
		this.parent = g;
	}
	
	private synchronized void initLine(Color col) {
		this.color = col;
		curPt = 0;
	}
	
	private synchronized  int nextPtIndex() {
		return (this.curPt + 1) % this.points.length;
	}
	
	private synchronized int nextPtIndex(int n) {
		return (n + 1) % this.points.length;
	}
	
	private synchronized int ptNIndex(int n) {
		return (this.curPt + n) % this.points.length;
	}
}
