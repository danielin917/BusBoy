/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.dataGenerators;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/**
 *
 * @author aaresh
 */
public class RandomGenerator implements DataGenerator {

	private JPanel panel;
	
	private JSpinner maxSpinner;
	private JSpinner minSpinner;
	private JSpinner periodSpinner;
	private JSpinner dutyCycleSpinner;
	
	private Random rand;
	
	@Override
	public double next(long time) {
		return rand.nextDouble() * (Util.doubleJSpinnerValue(maxSpinner) - Util.doubleJSpinnerValue(minSpinner)) + 
				Util.doubleJSpinnerValue(minSpinner);
	}

	@Override
	public void generatePanel() {
		panel = new JPanel();
		
		maxSpinner = Util.newFullRangeDoubleJSpinner();
		minSpinner = Util.newFullRangeDoubleJSpinner();
		
		Util.setJSpinnerWidth(maxSpinner, 200);
		Util.setJSpinnerWidth(minSpinner, 200);
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = 1; c.gridheight = 1;
		
		c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Max: " ), c);
		c.gridx = 1; c.gridy = 0; panel.add(maxSpinner, c);
		c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Min: " ), c);
		c.gridx = 1; c.gridy = 1; panel.add(minSpinner, c);
		
		rand = new Random(System.currentTimeMillis());
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}
	
	@Override
	public String toString() {
		return "Random number";
	}
}
