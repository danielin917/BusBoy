/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.dataGenerators;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/**
 *
 * @author aaresh
 */
public class SquareWaveGenerator2 implements DataGenerator {

	private JPanel panel;
	
	private JSpinner maxSpinner;
	private JSpinner minSpinner;
	private JSpinner periodSpinner;
	private JSpinner dutyCycleSpinner;
	
	@Override
	public double next(long time) {
		double time_d = (time / 1000.0) % Util.doubleJSpinnerValue(periodSpinner);
		if(time_d / Util.doubleJSpinnerValue(periodSpinner) < 
				(Util.doubleJSpinnerValue(dutyCycleSpinner) / 100.0) ) {
			return Util.doubleJSpinnerValue(maxSpinner);
		} else {
			return Util.doubleJSpinnerValue(minSpinner);
		}
	}

	@Override
	public void generatePanel() {
		panel = new JPanel();
		
		maxSpinner = Util.newFullRangeDoubleJSpinner();
		minSpinner = Util.newFullRangeDoubleJSpinner();
		periodSpinner = Util.newFullRangeDoubleJSpinner();
		dutyCycleSpinner = Util.newFullRangeDoubleJSpinner();
		
		Util.setJSpinnerWidth(maxSpinner, 200);
		Util.setJSpinnerWidth(minSpinner, 200);
		Util.setJSpinnerWidth(periodSpinner, 200);
		Util.setJSpinnerWidth(dutyCycleSpinner, 200);
		dutyCycleSpinner.getModel().setValue(50.0);
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = 1; c.gridheight = 1;
		
		c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Max: " ), c);
		c.gridx = 1; c.gridy = 0; panel.add(maxSpinner, c);
		c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Min: " ), c);
		c.gridx = 1; c.gridy = 1; panel.add(minSpinner, c);
		c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Period (s): " ), c);
		c.gridx = 1; c.gridy = 2; panel.add(periodSpinner, c);
		c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Duty cycle (%): " ), c);
		c.gridx = 1; c.gridy = 3; panel.add(dutyCycleSpinner, c);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public String toString() {
		return "Square wave 2 (max, min, period, duty cycle)";
	}
}
