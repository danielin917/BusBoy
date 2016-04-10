/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.dataGenerators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author aaresh
 */
public class Constant implements DataGenerator {

	private JPanel panel;
	private JSpinner spinner;
	
	public Constant() {
		
	}
	
	@Override
	public double next(long time) {
		return (double) spinner.getModel().getValue();
	}

	@Override
	public void generatePanel() {
		panel = new JPanel();
		panel.add(new JLabel("Value:"));
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		spinner = Util.newFullRangeDoubleJSpinner();
		Util.setJSpinnerWidth(spinner, 200);
		
		panel.add(spinner);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public String toString() {
		return "Constant";
	}
	
}
