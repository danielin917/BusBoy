/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.dataGenerators;

import java.awt.Dimension;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author aaresh
 */
public class Util {
	public static JSpinner newFullRangeDoubleJSpinner() {
		return new JSpinner(new SpinnerNumberModel(0, -1*Double.MAX_VALUE, Double.MAX_VALUE, 1));
	}
	
	public static void setJSpinnerWidth(JSpinner spinner, int width) {
		spinner.setPreferredSize(new Dimension(width, spinner.getPreferredSize().height));
	}
	
	public static double doubleJSpinnerValue(JSpinner spinner) {
		return (double) spinner.getModel().getValue();
	}
}
