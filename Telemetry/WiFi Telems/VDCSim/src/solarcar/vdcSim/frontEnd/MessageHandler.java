/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.vdcSim.frontEnd;

import java.awt.Dimension;
import java.io.Serializable;
import javax.sound.midi.Soundbank;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import solarcar.vdcSim.CANStructureParser;
import solarcar.vdcSim.CellRenderer;
import solarcar.vdcSim.Message;

/**
 *
 * @author aaresh
 */
public class MessageHandler implements ListSelectionListener, Serializable {
	
	private JList<Message> messageList;
	private JPanel messagePanel;
	
	private static MessageHandler mh = null;
	
	public JList<Message> getMessageList() {
		return messageList;
	}

	public static MessageHandler get() {
		if(mh == null) {
			mh = new MessageHandler();
			mh.start();
			System.out.println("created new message handler");
		}
		return mh;
	}
	
	protected static void replace(JList<Message> msglist, JPanel msgpanel) {
		mh = null;
		mh = new MessageHandler(msglist, msgpanel);
		System.out.println("swapped message handler");
	}
	
	private MessageHandler(JList<Message> msglist, JPanel msgpanel) {
		messageList = msglist;
		messagePanel = msgpanel;
	}
	
	private MessageHandler() {
		messageList = new JList<>();
		CANStructureParser csp = new CANStructureParser("canstructure.xml");
		csp.parse();
		messageList.setModel(csp.getListModel());
		messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messageList.setCellRenderer(new CellRenderer());
		
		messagePanel = new JPanel();
		messagePanel.setPreferredSize(new Dimension(805, 400));
		messagePanel.add(new JPanel());
	}
	
	public void start() {
		messageList.addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting() == false) {
			messagePanel.remove(messagePanel.getComponent(0));
			if(messageList.getSelectedValue().getPanel() == null)
				messageList.getSelectedValue().generatePanel();
			messagePanel.add(messageList.getSelectedValue().getPanel());
			
			messagePanel.revalidate();
			messagePanel.repaint();
		}
	}

	public JPanel getMessagePanel() {
		return messagePanel;
	}
}

