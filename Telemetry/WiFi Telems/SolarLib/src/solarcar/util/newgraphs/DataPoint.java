/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.newgraphs;


/**
 * A class which is used to store data points for graphs. 
 * @author aaresh
 */
class DataPoint {
	
	int min;
	int max;
	int sum;
	int samples;
	int defaultMean;
	
	/**
	 * Creates a new <code>DataPoint</code> object with an initial 
	 * value of 0. 
	 */
	public DataPoint() {
		defaultMean = 0;
		reset();
	}
	
	/**
	 * Add a new point to the previously collected data;
	 * @param p The new point
	 */
	public void AddPoint(int p) {
		sum += p;
		samples++;
		if(p > max) max = p;
		else if(p < min) min = p;
	}
	
	// TODO: throw an exception if there are no samples
	/**
	 * Returns the mean of all added points.
	 * @return The mean of all added points
	 */
	public int getMean() {
		if(samples == 0) return defaultMean;
		return sum / samples;
	}

	/**
	 * Returns the minimum of all added points.
	 * @return The minimum of all added points
	 */
	public int getMin() {
		return this.min;
	}
	
	/**
	 * Returns the maximum of all added points.
	 * @return The maximum of all added points
	 */
	public int getMax() {
		return this.max;
	}
	
	/**
	 * Sets the default mean to use if there are no points collected
	 * @param mean 
	 */
	public void setDefaultMean(int mean) {
		this.defaultMean = mean;
	}
	
	/**
	 * Resets the data in the object 
	 */
	public final void reset() {
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		samples = 0;
	}
}
