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
public class SineGenerator implements DataGenerator {

	private JPanel panel;
	
	private JSpinner amplitudeSpinner;
	private JSpinner offsetSpinner;
	private JSpinner periodSpinner;
	private JSpinner phaseSpinner;
	
	@Override
	public double next(long time) {
		if(Util.doubleJSpinnerValue(periodSpinner) != 0) 
			return Util.doubleJSpinnerValue(amplitudeSpinner) * 
					Math.sin( (2 * Math.PI / Util.doubleJSpinnerValue(periodSpinner)) *
								( (time / 1000.0) + Util.doubleJSpinnerValue(phaseSpinner) ) ) + 
					Util.doubleJSpinnerValue(offsetSpinner);
		else 
			return Util.doubleJSpinnerValue(offsetSpinner);
	}

	@Override
	public void generatePanel() {
		panel = new JPanel();
		
		amplitudeSpinner = Util.newFullRangeDoubleJSpinner();
		offsetSpinner = Util.newFullRangeDoubleJSpinner();
		periodSpinner = Util.newFullRangeDoubleJSpinner();
		phaseSpinner = Util.newFullRangeDoubleJSpinner();
		
		Util.setJSpinnerWidth(amplitudeSpinner, 200);
		Util.setJSpinnerWidth(offsetSpinner, 200);
		Util.setJSpinnerWidth(periodSpinner, 200);
		Util.setJSpinnerWidth(phaseSpinner, 200);
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = 1; c.gridheight = 1;
		
		c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Amplitude: " ), c);
		c.gridx = 1; c.gridy = 0; panel.add(amplitudeSpinner, c);
		c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Offset: " ), c);
		c.gridx = 1; c.gridy = 1; panel.add(offsetSpinner, c);
		c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Period (s): " ), c);
		c.gridx = 1; c.gridy = 2; panel.add(periodSpinner, c);
		c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Phase (s): " ), c);
		c.gridx = 1; c.gridy = 3; panel.add(phaseSpinner, c);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public String toString() {
		return "Sine wave";
	}
}
