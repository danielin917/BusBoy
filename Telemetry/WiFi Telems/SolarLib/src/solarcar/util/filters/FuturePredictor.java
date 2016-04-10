/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.filters;

/**
 * An interface for creating classes which attempt to predict the
 * future. 
 * 
 * @author aaresh
 */
public interface FuturePredictor {
	
	/**
	 * Tells the FuturePredictor what the value is at the current
	 * point in time. 
	 * 
	 * @param p		The point recorded at the current time
	 * 
	 */
	void addPoint(double p);
	
	/**
	 * Get the future prediction at a specified time in the future. 
	 * Example: getPredictionAt(1000) returns the prediction one second in
	 * the future. 
	 * 
	 * @param time	The time in milliseconds
	 * @return		The prediction at time milliseconds in the future 
	 */
	double getPredictionAt(long time);
}
