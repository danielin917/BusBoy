/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util.newgraphs;

import java.awt.Color;

/**
 *
 * @author aaresh
 */
public class GraphColorScheme {
	public Color xAxisColor;
	public Color yAxisColor;
	public Color xAxisLabelColor;
	public Color yAxisLabelColor;
	public Color minorTickColor;
	public Color majorTickColor;
	public Color[] lineColors;

	public GraphColorScheme(Color xAxisColor, Color yAxisColor, Color xAxisLabelColor, Color yAxisLabelColor, Color minorTickColor, Color majorTickColor, Color[] lineColors) {
		this.xAxisColor = xAxisColor;
		this.yAxisColor = yAxisColor;
		this.xAxisLabelColor = xAxisLabelColor;
		this.yAxisLabelColor = yAxisLabelColor;
		this.minorTickColor = minorTickColor;
		this.majorTickColor = majorTickColor;
		this.lineColors = lineColors;
	}
	
	public GraphColorScheme() {
		this.xAxisColor = new Color(130, 130, 255);
		this.yAxisColor = xAxisColor;
		this.xAxisLabelColor = Color.black;
		this.yAxisLabelColor = xAxisLabelColor;
		this.majorTickColor = new Color(0,255,0);
		this.minorTickColor = majorTickColor;
		this.lineColors = new Color[] {
			Color.black,
			Color.blue,
			Color.red,
			Color.darkGray,
			Color.yellow
		};
	}
}
