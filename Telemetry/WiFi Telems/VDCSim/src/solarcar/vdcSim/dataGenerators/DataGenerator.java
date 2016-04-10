/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.dataGenerators;

import javax.swing.JPanel;

/**
 *
 * @author aaresh
 */
public interface DataGenerator {
	public double next(long time);
	public void generatePanel();
	public JPanel getPanel();
}
