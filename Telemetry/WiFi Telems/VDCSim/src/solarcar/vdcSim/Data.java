/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import solarcar.vdcSim.dataGenerators.Constant;
import solarcar.vdcSim.dataGenerators.DataGenerator;
import solarcar.vdcSim.dataGenerators.RandomGenerator;
import solarcar.vdcSim.dataGenerators.SineGenerator;
import solarcar.vdcSim.dataGenerators.SquareWaveGenerator;
import solarcar.vdcSim.dataGenerators.SquareWaveGenerator2;

/**
 *
 * @author aaresh
 */
public class Data implements ActionListener {
	float value;
	String name;
	
	JPanel panel = null;
	JPanel genPanel = null;
	
	DataGenerator generators[] = {	new Constant(), 
									new SineGenerator(), 
									new SquareWaveGenerator(), 
									new SquareWaveGenerator2(), 
									new RandomGenerator()
								}; 
	JComboBox<DataGenerator> generatorList;
	
	public Data(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void generatePanel() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		genPanel = new JPanel();
		panel.add(genPanel, BorderLayout.CENTER);
		
		generatorList = new JComboBox<>(generators);
		generatorList.addActionListener(this);
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(generatorList, BorderLayout.CENTER);
		listPanel.add(new JLabel("Value generator: "), BorderLayout.WEST);
		panel.add(listPanel, BorderLayout.NORTH);
		
		if(selectedGenerator().getPanel() == null) {
			selectedGenerator().generatePanel();
		}
		genPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		genPanel.add(selectedGenerator().getPanel());
		
		// TODO: create data panel
	}
	
	private DataGenerator selectedGenerator() {
		return (DataGenerator) generatorList.getSelectedItem();
	}
	
	public double next(long time) {
		return selectedGenerator().next(time);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		genPanel.remove(genPanel.getComponent(0));
		if(selectedGenerator().getPanel() == null) {
			selectedGenerator().generatePanel();
		}
		genPanel.add(selectedGenerator().getPanel());
		
		genPanel.revalidate();
		genPanel.repaint();
	}
}
