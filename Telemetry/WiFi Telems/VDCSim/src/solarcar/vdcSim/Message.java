/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import solarcar.vdcPublisher.ListenerManager;

/**
 *
 * @author aaresh
 */

// TODO: add send once button
public class Message implements ActionListener, ChangeListener{

	private Data[] data;
	private double frequency;
	private boolean running;
	private String id;
	private int addr;
	private long startTime;
	
	private Timer timer;
	
	private JPanel panel = null;
	private JPanel optionsPanel = null;
	private JTabbedPane dataTabs = null;
	private JToggleButton enableButton = null;
	private JCheckBox defaultFrequencyCheckbox = null;
	private JSpinner frequencySpinner = null;

	public JPanel getPanel() {
		return panel;
	}
	
	public void generatePanel() {
		panel = new JPanel();
		optionsPanel = new JPanel();
		dataTabs = new JTabbedPane();
		
		panel.setLayout(new BorderLayout());
		panel.add(optionsPanel, BorderLayout.NORTH);
		panel.add(dataTabs, BorderLayout.SOUTH);
		
		optionsPanel.setPreferredSize(new Dimension(800, 50));
		dataTabs.setPreferredSize(new Dimension(800, 340));
		
		
		for(Data d : data) {
			d.generatePanel();
			dataTabs.add(d.toString(), d.getPanel());
			d.getPanel().setPreferredSize(new Dimension(790, 320));
		}
		
		optionsPanel.setLayout(new FlowLayout());
		
		enableButton = new JToggleButton("Enable");
		defaultFrequencyCheckbox = new JCheckBox("Use default freqency");
		frequencySpinner = new JSpinner(new SpinnerNumberModel(Options.defaultFrequency, 0.001, 1000, 1));
		
		defaultFrequencyCheckbox.setSelected(true);
		frequencySpinner.setEnabled(!defaultFrequencyCheckbox.isSelected());
		
		enableButton.addActionListener(this);
		defaultFrequencyCheckbox.addActionListener(this);
		frequencySpinner.addChangeListener(this);
		
		optionsPanel.add(enableButton);
		optionsPanel.add(defaultFrequencyCheckbox);
		optionsPanel.add(new JLabel("Frequency (hz): "));
		optionsPanel.add(frequencySpinner);
		
	}
	
	public Message(String id, int addr, Data[] data) {
		this.id = id;
		this.data = data;
		this.addr = addr;
		this.frequency = Options.defaultFrequency;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public void enable() {
		running = true;
		enableButton.setText("disable");
		timer = new Timer();
		timer.scheduleAtFixedRate(new SendThread(), 100, (long) (1000 / this.frequency));
		startTime = System.currentTimeMillis();
	}
	
	public void disable() {
		running = false;
		enableButton.setText("enable");
		if(timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		// TODO: implement
	}
	
	public boolean isEnabled() {
		return running;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == enableButton)
		{
			if(enableButton.isSelected()) {
				enable();
			} else {
				disable();
			}
		} else if(e.getSource() == defaultFrequencyCheckbox) {
			frequencySpinner.setEnabled(!defaultFrequencyCheckbox.isSelected());
			if(defaultFrequencyCheckbox.isSelected()) {
				setFrequency( Options.defaultFrequency );
			} else {
				setFrequency((double) frequencySpinner.getModel().getValue());
			}
		}
	}
	
	public void setFrequency(double freq) {
		disable();
		this.frequency = freq;
		if(enableButton.isSelected())
			enable();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setFrequency((double) frequencySpinner.getModel().getValue());
	}

	private class SendThread extends TimerTask {
		@Override
		public void run() {
			//System.out.println(this + "; ms=" + System.currentTimeMillis());
			String str = "data id='" + id + "' "; 
			for(Data d : data) {
				str += d.name + "='" + d.next(System.currentTimeMillis() - startTime) + "' ";
			}
			ListenerManager.get().sendAll(str);
			//System.out.println(str);
		}
	}
}
