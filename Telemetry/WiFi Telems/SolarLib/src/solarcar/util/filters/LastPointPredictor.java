/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.filters;

/**
 * A mostly useless <code>FuturePredictor</code>. It's mainly being 
 * used for testing. It simply predicts that the last data point it got 
 * will continue on forever. 
 * @author aaresh
 */
public class LastPointPredictor implements FuturePredictor {

	double lastPoint = 0;
	
	@Override
	public void addPoint(double p) {
		lastPoint = p;
	}

	@Override
	public double getPredictionAt(long time) {
		return lastPoint;
	}
	
}
