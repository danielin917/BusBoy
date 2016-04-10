/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.newgraphs;

// TODO: figure out which type of FuturePredictor will be used as default
// TODO: allow label coloring

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

// TODO: update javadoc - minor tick is now a dotted line running across the graph
// TODO: add option to modify minor tick stroke & major tick stroke
// TODO: speed/memory tradeoff of having array which stores where ticks go vs calculating each time

/**
 * A class which enables easy plotting of data over time.
 * <p>
 * This is a complete rewrite of the old graph implementation with a few
 * specific goals in mind:
 * <ul>
 * <li>Massively reduce memory footprint</li>
 * <li>Optimize processor utilization by sleeping more and only waking up
 *		to redraw when there is actually new data to redraw </li>
 * <li>Add more options and functionality </li>
 * <li>More code reuse. The old one had multiple classes which were at least
 *		80% the same, with some minor tweaks here and there. In order to change
 *		how something works in the graphs, you had to change it in multiple 
 *		places. </li>
 * </ul>
 * @author aaresh
 */
public class Graph extends JPanel {
	
	/*
	 * Initialized during construction
	 */
	private double yMax, yMin;
	private int startTime, endTime;
	private List<LineDrawer> drawers;
	private GraphColorScheme colorScheme;
	private String yMinLabel, yZeroLabel, yMaxLabel;
	private String startTimeLabel, endTimeLabel;
	private Stroke majorTickStroke, minorTickStroke;
	private Map<TextAttribute, Object> yZeroLabelAttributes;
	
	/*
	 * Initialized after size is determined
	 */
	private Dimension panelSize = null;
	private Dimension graphSize = null;
	private int updateInterval;
	protected int pastPixels;		// number of pixels which correspond to the past
	protected int futurePixels;	// number of pxiels which correspond to the future
								// Total width = pastPixels + futurePixels
	private double yScale;		// scale in px / unit 
	private int majorTickPxInterval;	// How many pixels between major ticks
	private int minorTickPxInterval;	// How many pixels between minor ticks
	private double majorTickInterval;
	private double minorTickInterval;
	private int yZeroPx;				// The pixel location of y = 0;
	private Point endTimeLabelLoc;
	
	/*
	 * Final objects
	 */
	private final Object lock = new Object();
	private final int bottomBorder = 15;
	
	/**
	 * Creates a new graph object to plot a single line. 
	 * The graph can plot the past and future predictions. If the 
	 * graph is asked to perform future predictions, it will do so
	 * using the default <code>FuturePredictor</code> (no useful ones 
	 * have been written at the time of documentation, so currently it
	 * just predicts the last point will continue on forever). 
	 * <p>
	 * Examples:
	 * <br/>
	 * <code>Graph(0, 1000, -600, 0)</code> creates a graph which can plot values from
	 * 0 to 1000, and plots the past 10 minutes of data.
	 * <br/>
	 * <code>Graph(-100, 100, -3600, 600)</code> creates a graph which can plot values 
	 * from -100 to 100, and plots the past 1 hour of data and 
	 * predictions up to 10 minutes into the future. 
	 * @param yMin			The minimum y value to display
	 * @param yMax			The maximum y value to display
	 * @param startTime		The time in seconds from the present to start the graph at
	 * @param endTime		The time in seconds from the present to end the graph at
	 */
	// TODO: test
	public Graph(double yMin, double yMax, int startTime, int endTime) {
		this.drawers = new ArrayList<>(1);
		if(endTime > 0) {
			this.drawers.add(new FutureLineDrawer());
		} else {
			this.drawers.add(new LineDrawer());
		}
		
		initGraph(yMin, yMax, startTime, endTime, this.drawers);
	}
	
	/**
	 * Creates a new graph object to plot a single line using the
	 * specified LineDrawer. 
	 * The graph can plot the past and future predictions. 
	 * <p>
	 * Examples:
	 * <br/>
	 * <code>Graph(0, 1000, -600, 0, new LineDrawer())</code> creates a 
	 * graph which can plot values from 0 to 1000, and plots the past 
	 * 10 minutes of data using a standard <code>LineDrawer</code>. 
	 * <br/>
	 * <code>Graph(-100, 100, -3600, 600, new FutureLineDrawer(new 
	 * LastPointPredictor()))</code> creates a graph which can plot values 
	 * from -100 to 100, and plots the past 1 hour of data and 
	 * predictions up to 10 minutes into the future. The predictions are 
	 * made using the <code>LastPointPredictor</code>
	 * @param yMin			The minimum y value to display
	 * @param yMax			The maximum y value to display
	 * @param startTime		The time in seconds from the present to start the graph at
	 * @param endTime		The time in seconds from the present to end the graph at
	 * @param drawer		The specific LineDrawer to use to draw the line 
	 */
	// TODO: test
	public Graph(double yMin, double yMax, int startTime, int endTime, LineDrawer drawer) {
		this.drawers = new ArrayList<>(1);
		this.drawers.add(drawer);
		
		initGraph(yMin, yMax, startTime, endTime, this.drawers);
	}
	
	/**
	 * Creates a new graph object to plot a certain number of lines.
	 * The graph can plot the past and future predictions. If the 
	 * graph is asked to perform future predictions, it will do so
	 * using the default <code>FuturePredictor</code> (no useful ones 
	 * have been written at the time of documentation, so currently it
	 * just predicts the last point will continue on forever). 
	 * <p>
	 * Examples:
	 * <br/>
	 * <code>Graph(0, 1000, -600, 0, 3)</code> creates a 
	 * graph which can plot values from 0 to 1000, and plots the past 
	 * 10 minutes of data. This graph can plot 3 lines. 
	 * <br/>
	 * <code>Graph(-100, 100, -3600, 600, 2)</code> creates a graph 
	 * which can plot values from -100 to 100, and plots the past 1 hour 
	 * of data and predictions up to 10 minutes into the future. This 
	 * graph can plot 2 lines, and will make individual predictions for
	 * each line. 
	 * @param yMin			The minimum y value to display
	 * @param yMax			The maximum y value to display
	 * @param startTime		The time in seconds from the present to start the graph at
	 * @param endTime		The time in seconds from the present to end the graph at
	 * @param numLines		The number of lines to draw
	 */
	// TODO: test
	public Graph(double yMin, double yMax, int startTime, int endTime, int numLines) {
		this.drawers = new ArrayList<>(numLines);
		
		if(endTime > 0) {
			for(int i = 0; i < numLines; i++) {
				this.drawers.add(new FutureLineDrawer());
			}
		} else {
			for(int i = 0; i < numLines; i++) {
				this.drawers.add(new LineDrawer());
			}
		}
		
		initGraph(yMin, yMax, startTime, endTime, this.drawers);
	}
	
	/**
	 * Creates a new graph object to plot one or more lines using
	 * a specific list of LineDrawers. This is best for plotting multiple
	 * lines which make future predictions, as you have fine control over 
	 * the predictors for reach individual line. The <code>drawers</code>
	 *  can be a mix of any type of <code>LineDrawer</code>.
	 * @param yMin			The minimum y value to display
	 * @param yMax			The maximum y value to display
	 * @param startTime		The time in seconds from the present to start the graph at
	 * @param endTime		The time in seconds from the present to end the graph at
	 * @param drawers		A list of LineDrawers to use 
	 */
	// TODO: test
	public Graph(double yMin, double yMax, int startTime, int endTime, List<LineDrawer> drawers) {
		initGraph(yMin, yMax, startTime, endTime, drawers);
	}
	
	/** 
	 * Starts the graph displaying data 
	 */
	public void start() {
		// TODO: Implement start;
		Timer t = new Timer("Graph timer");
		t.scheduleAtFixedRate(new SizeChecker(this), 0, 500);
//		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/** 
	 * Draws lines on the graph denoting the specified interval. 
	 * Major tick lines are drawn over the minor ticks. The major tick
	 * interval does not need to be a multiple of the minor tick interval,
	 * but that would make the most sense in most cases. 
	 * @param interval	The major tick interval 
	 * @return			Returns the number of ticks/lines which will be drawn
	 */
	public int setMajorTickInterval(double interval) {
		this.majorTickPxInterval = Math.abs(this.getPixelY(majorTickInterval) - this.yZeroPx);
		this.majorTickInterval = Math.abs(interval);
//		System.out.println("Set major tick interval to: " + this.majorTickPxInterval);
		
		// TODO: return something useful
		return 0;
	}
	
	/* TODO: take the speed hit & increase accuracy of lines drawn by 
	 * saving the y interval to increase rather than the pixel interval to 
	 * increase. Saving the pixel interval can cause edge cases where the 
	 * line drawn for one of the ticks is not where it's supposed to be 
	 * (eg. 2.3 pixels rounds to 2 pixels, 2 + 2 = 4, while 2.3 + 2.3 = 4.6 
	 * which rounds to 5, and thus ends up falling a pixel short). 
	 */
	/**
	 * Draws ticks on the y-axis denoting the specified interval.
	 * Major tick lines are drawn over the minor ticks. The major tick
	 * interval does not need to be a multiple of the minor tick interval,
	 * but that would make the most sense in most cases. 
	 * @param interval	The minor tick interval
	 * @return			Returns the number of ticks which will be drawn
	 */
	public int setMinorTickInterval(double interval) {
		this.minorTickPxInterval = Math.abs(this.getPixelY(minorTickInterval) - this.yZeroPx);
		this.minorTickInterval = Math.abs(interval);
		// TODO: return something useful
		return 0;
	}
	
	/** 
	 * Draws the specified number of lines on the graph, at evenly
	 * spaced intervals. One line will be drawn at the top; this line is
	 * included in the number of ticks (ex: 
	 * <code>setNumberOfMajorTicks(1)</code> would draw one line at 
	 * the top. <code>setNumberOfMajorTicks(2)</code> would draw two 
	 * lines, one at the top and one in the middle. 
	 * Major tick lines are drawn over minor ticks. The 
	 * number of minor ticks does not have to be a multiple of the number
	 * of major ticks, but that would make the most sense in most cases. 
	 * @param ticks		The number of lines/ticks to draw
	 * @return			The interval of each tick 
	 */
	public double setNumberOfMajorTicks(int ticks) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Draws the specified number of ticks on the y-axis at 
	 * evenly spaced intervals. 
	 * Major tick lines are drawn over minor ticks. The 
	 * number of minor ticks does not have to be a multiple of the number
	 * of major ticks, but that would make the most sense in most cases. 
	 * @param ticks		The number of ticks to draw
	 * @return			The interval of each tick 
	 */
	public double setNumberOfMinorTicks(int ticks) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Sets the color of both the x- and y-axes. 
	 * Default color is RGB 130,130,255 (medium blue)
	 * @param col	The color to draw the x- and y-axes
	 */
	public void setAxisColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/** 
	 * Sets the color of the x axis. 
	 * Default color is RGB 130,130,255 (medium blue)
	 * @param col	The color to draw the x-axis
	 */
	public void setXAxisColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Sets the color of the y axis.
	 * Default color is RGB 130,130,255 (medium blue)
	 * @param col	The color to draw the y-axis
	 */
	public void setYAxisColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Sets the colors of both the major and minor ticks. 
	 * Major ticks default to Green (RGB 0,255,0) and minor ticks
	 * default to RGB 130,130,255 (medium blue)
	 * @param col	The color to draw the major and minor ticks
	 */
	public void setTickColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Sets the color of the major ticks. 
	 * Major ticks default to Green (RGB 0,255,0). 
	 * @param col	The color to draw major ticks. 
	 */
	public void setMajorTickColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	
	/**
	 * Sets the color of minor ticks. 
	 * Minor ticks default to RGB 130,130,255 (medium blue)
	 * @param col	The color to draw minor ticks
	 */
	public void setMinorTickColor(Color col) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Adds another line to the existing list of lines to draw. 
	 * @param drawer	The new line to draw
	 */
	public void AddLineDrawer(LineDrawer drawer) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/** 
	 * Add multiple new lines to the existing list of lines to
	 * draw. 
	 * @param drawers	The list of new lines to draw
	 */
	public void AddLineDrawers(List<LineDrawer> drawers) {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Add a new data point to plot. 
	 * The data point is assumed to be at the current time. 
	 * @param point		The new data point to plot 
	 */
	public void addPoint(double point) {
		// TODO: test;
		drawers.get(0).addPoint(point);
	}
	
	/**
	 * Adds a new data point to plot on the specified line. 
	 * @param line		The line to add the data point to 
	 * @param point		The new data point to plot 
	 */
	public void addPoint(int line, double point) {
		// TODO: test;
		drawers.get(line).addPoint(point);
	}
	
	/**
	 * Get the object corresponding to a specific line. 
	 * @param n		The number of the requested line
	 * @return		The object corresponding the requested line 
	 */
	public LineDrawer getLineDrawer(int n) {
		return this.drawers.get(n);
	}
	
	/**
	 * Get the number of lines being drawn. 
	 * @return		The number of lines which are drawn on the graph. 
	 */
	public int getNumberOfLines() {
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns a string with the properly formatted elapsed time.
	 * This is used to create the strings showing start and end times on the graph
	 * since none of the built in Java functions work as desired. 
	 * @param seconds
	 * @return 
	 */
	private String formatTime(int seconds) {
		String retv = "";
		
		if(seconds < 0) {
			retv += "-";
			seconds *= -1;
		}
		
		if(seconds >= 3600) {
			retv += ((int) (seconds / 3600)) + ":";
		}
		
		retv += ((((int) ((seconds % 3600) / 60)) < 10) ? "0" : "") + 
				((int) ((seconds % 3600) / 60)) + 
				":" + 
				(((int) (seconds % 60) < 10) ? "0" : "") + 
				(seconds % 60);
		
		return retv;
	}
	
	/**
	 * Initializes the graph's internal state. 
	 * @param yMin			The minimum y value to display
	 * @param yMax			The maximum y value to display
	 * @param startTime		The time in seconds from the present to start the graph at
	 * @param endTime		The time in seconds from the present to end the graph at
	 * @param drawers		A list of LineDrawers to use 
	 */
	private void initGraph(double yMin, double yMax, int startTime, int endTime, List<LineDrawer> drawers) {
		// TODO: Finish implementing initGraph
		this.yMax = yMax;
		this.yMin = yMin;
		this.startTime = startTime;
		this.endTime = endTime;
		this.drawers = drawers;
		this.colorScheme = new GraphColorScheme();
		
		DecimalFormat df = new DecimalFormat("0.##");
		yMinLabel = df.format(this.yMin);
		yZeroLabel = "0";
		yMaxLabel = df.format(this.yMax);
		
		this.startTimeLabel = this.formatTime(this.startTime);
		this.endTimeLabel = this.formatTime(this.endTime);
		
		float dash[] = {1.0f, 1.0f};
		this.minorTickStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		this.majorTickStroke = new BasicStroke();
		
		yZeroLabelAttributes = new HashMap<>();
		yZeroLabelAttributes.put(TextAttribute.BACKGROUND, this.getBackground());
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		double y; 
		int yPx;
		
		Graphics2D g2d = (Graphics2D) g;
		
		synchronized(this.lock) {
			// Draw minor ticks
			g2d.setColor(this.colorScheme.minorTickColor);
			g2d.setStroke(this.minorTickStroke);
			for(y = -this.minorTickInterval, yPx = getPixelY(y); 
					yPx <= this.graphSize.height; 
					y -= this.minorTickInterval, yPx = getPixelY(y)) {
				g2d.drawLine(0, yPx, this.graphSize.width, yPx);
			}


			for(y = this.minorTickInterval, yPx = getPixelY(y); 
					yPx >=0; 
					y += this.minorTickInterval, yPx = getPixelY(y)) {
				g2d.drawLine(0, yPx, this.graphSize.width, yPx);
			}


			// Draw major ticks
			g2d.setColor(this.colorScheme.majorTickColor);
			g2d.setStroke(this.majorTickStroke);
			for(y = -this.majorTickInterval, yPx = getPixelY(y); 
					yPx <= this.graphSize.height; 
					y -= this.majorTickInterval, yPx = getPixelY(y)) {
				g2d.drawLine(0, yPx, this.graphSize.width, yPx);
			}


			for(y = this.majorTickInterval, yPx = getPixelY(y); 
					yPx >=0; 
					y += this.majorTickInterval, yPx = getPixelY(y)) {
				g2d.drawLine(0, yPx, this.graphSize.width, yPx);
			}


			// Draw X axis
			g2d.setColor(this.colorScheme.xAxisColor);
			g2d.drawLine(0, this.yZeroPx, this.graphSize.width, this.yZeroPx);

			// Draw Y axis
			g2d.setColor(this.colorScheme.yAxisColor);
			g2d.drawLine(this.pastPixels - 1, this.graphSize.height - 1, 
						 this.pastPixels - 1, 0);

			// Draw labels
			g2d.setColor(this.colorScheme.xAxisLabelColor);
			g2d.drawString(this.startTimeLabel, 0, this.panelSize.height - 2);
			g2d.drawString(this.endTimeLabel, this.endTimeLabelLoc.x, this.endTimeLabelLoc.y);

			g2d.setColor(this.colorScheme.yAxisLabelColor);
			g2d.drawString(this.yMinLabel, 0, this.graphSize.height - 2);
			g2d.drawString(this.yMaxLabel, 0, 11);
			g2d.setFont(g2d.getFont().deriveFont(this.yZeroLabelAttributes));
			g2d.drawString(this.yZeroLabel, 0, this.yZeroPx + 5);

			// TODO: Draw lines
			for(LineDrawer ld : this.drawers) {
				ld.drawLine(g2d);
			}
		}
	}

	
	/**
	 * Get the pixel location in the y direction for the specified 
	 * data point. 
	 * @param data	The data point to plot
	 * @return		The y coordinate of the graphics object at which to plot the data
	 */
	protected int getPixelY(double data) {
		// TODO: test;
		return (int)(this.graphSize.height - ( this.yScale * ( data - yMin )));
	}
	
	/**
	 * 
	 */
	private class SizeChecker extends TimerTask {
		Graph g;
		
		SizeChecker(Graph g) {
			this.g = g;
		}
		
		@Override
		public void run() {
			synchronized(g.lock) {
				g.panelSize = g.getSize();
				System.out.println("Panel size: " + g.panelSize);
				if(g.panelSize.height == 0 || g.panelSize.width == 0) {
					g.panelSize = null;
				} else {
					this.cancel();
					Timer t = new Timer("Graph repainter");

					g.graphSize = new Dimension(g.panelSize);
					g.graphSize.height -= bottomBorder;
					
					g.updateInterval = ((g.endTime - g.startTime) * 1000) / g.graphSize.width;
					g.yScale = g.graphSize.height / (g.yMax - g.yMin);

					g.pastPixels = (startTime * -1000) / (g.updateInterval);
					g.futurePixels = g.graphSize.width - pastPixels;
					
					g.yZeroPx = g.getPixelY(0);
					
					double largerSide = ( ( g.yMax > (-1*g.yMin) ) ? g.yMax : -1*g.yMin );
					g.majorTickInterval = largerSide / 2;
					g.setMajorTickInterval(g.majorTickInterval);
					
					g.minorTickInterval = largerSide / 4;
					g.setMinorTickInterval(g.minorTickInterval);
					
					g.endTimeLabelLoc = new Point(
							g.panelSize.width - (g.getFontMetrics(g.getFont()).stringWidth(g.endTimeLabel)) - 1, 
							g.panelSize.height - 2);
					
					for(LineDrawer ld : g.drawers) {
						ld.setGraphParent(g);
					}
					
					System.out.println("Repainting every: " + g.updateInterval + "ms");
					t.scheduleAtFixedRate(new Repainter(g), 0, g.updateInterval);
				}
			}
		}
	}
	
	private class Repainter extends TimerTask {
		Graph g;		
		
		public Repainter(Graph g) {
			this.g = g;
		}
		
		@Override
		public void run() {
			// TODO: tell lines to clamp and repaint
			for(LineDrawer ld : g.drawers) {
				ld.advancePixel();
			}
			g.repaint();
		}
		
	}
}
