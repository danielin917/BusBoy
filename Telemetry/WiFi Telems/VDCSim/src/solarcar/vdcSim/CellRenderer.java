/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author aaresh
 */
public class CellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		/*if (index % 2 == 0) {
			c.setBackground(Color.yellow);
		} else {
			c.setBackground(Color.white);
		}*/
		
		if( value instanceof Message && ((Message) value).isEnabled() ) {
			c.setBackground(Color.yellow);
		} else if (isSelected) {
			c.setBackground(new Color(135, 206, 250));
		} else {
			c.setBackground(Color.white);
		}
		
		return c;
	}
}