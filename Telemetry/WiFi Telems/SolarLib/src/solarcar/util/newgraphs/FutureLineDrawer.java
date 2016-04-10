/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.newgraphs;

// TODO: figure out default FuturePredictor & update documentation

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import solarcar.util.filters.FuturePredictor;


/**
 * An implementation of LineDrawer which allows drawing lines which 
 * predict future trends. 
 * @author aaresh
 */
public class FutureLineDrawer extends LineDrawer {
	
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the default
	 * color (black) and the default <code>FuturePredictor</code>. 
	 * No useful <code>FuturePredictor</code> has been written at the 
	 * time of documentation, so it currently uses LastPointPredictor which 
	 * simply predicts that the last point on the graph will continue on 
	 * forever. 
	 */
	public FutureLineDrawer() {
		super();
		// TODO: Implement;
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the default
	 * color (black) and the specified <code>FuturePredictor</code>. 
	 * @param predictor		The future prediction algorithm to use. 
	 */
	public FutureLineDrawer(FuturePredictor predictor) {
		super();
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the specified
	 * color and the default <code>FuturePredictor</code>. 
	 * No useful <code>FuturePredictor</code> has been written at the 
	 * time of documentation, so it currently uses LastPointPredictor which 
	 * simply predicts that the last point on the graph will continue on 
	 * forever. 
	 * @param col	The color to draw the line. 
	 */
	public FutureLineDrawer(Color col) {
		super(col);
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the specified
	 * colors and the default <code>FuturePredictor</code>. 
	 * No useful <code>FuturePredictor</code> has been written at the 
	 * time of documentation, so it currently uses LastPointPredictor which 
	 * simply predicts that the last point on the graph will continue on 
	 * forever. 
	 * @param past		The color to draw recorded past data 
	 * @param future	The color to draw future predictions 
	 */
	public FutureLineDrawer(Color past, Color future) {
		super(past);
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the specified
	 * color and <code>FuturePredictor</code>
	 * @param predictor		The future prediction algorithm to use
	 * @param col			The color to draw the line
	 */
	public FutureLineDrawer(FuturePredictor predictor, Color col) {
		super(col);
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new <code>FutureLineDrawer</code> using the specified
	 * colors and <code>FuturePredictor</code>
	 * @param predictor		The future prediction algorithm to use
	 * @param past			The color to draw recorded past data 
	 * @param future		The color to draw future predictions 
	 */
	public FutureLineDrawer(FuturePredictor predictor, Color past, Color future) {
		super(past);
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	protected void drawLine(Graphics2D g2d) {
		super.drawLine(g2d);
		// TODO: Implement;
		throw new UnsupportedOperationException("Not supported yet.");
	}

	
//	@Override
	protected void setGraphProperties(double yMin, double yMax, int startTime,
			int endTime, Dimension panelSize) {
		// TODO: Implement & adjust to latest version;
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
