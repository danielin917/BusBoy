package solarcar.gui.modules;

import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  PowerDeficietGraph extends SolarPanel implements DataMessageSubscriber {
	JLabel taxis;
	JLabel yaxis;
	MultiGraphPanel graph;
	private double curIn;
	private double curOut;
	private double voltMot1, voltMot2, voltBat;
	private double in;
	private double out;
	private SolarFilter filter;
	
	@Override
	public void init() {
		setBorder(new TitledBorder("Power Deficit Graph"));
		setPreferredSize(TelemetryGUI.HalfGraph);
		setLayout(new BorderLayout());
		taxis = new JLabel("Time");
		yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
		graph = new MultiGraphPanel(600, 20, 5, 2000, -2000, 2);
		graph.setColor(1, Color.BLUE);
		graph.start();
		
		add(taxis, BorderLayout.SOUTH);
		add(yaxis, BorderLayout.WEST);
		add(graph, BorderLayout.CENTER);
		
		taxis.setHorizontalAlignment(JLabel.CENTER);
		curIn = 0;
		curOut = 0;
		voltMot1 = 0;
                voltMot2 = 0;
		in = 0;
		out = 0;

		filter = new ExponentialFilter();
		String[] args = { Double.toString(0.005) };
		filter.setParams(args);
		
		MessageHandler.get().subscribeData("ab_current", this);
		MessageHandler.get().subscribeData("motbus", this);
                MessageHandler.get().subscribeData("motbus2", this);
                MessageHandler.get().subscribeData("micro_current", this);


	}
	
	@Override
	public void parseDataMessage(SolarDataMessage message) {
		switch (message.getId()) {
			case "ab_current":
				curIn = message.get("array");
                                voltBat = message.get("battery");
				break;
			case "motbus":
				voltMot1 = message.get("volt");
                                break;
                        case "motbus2":
				voltMot2 = message.get("volt");
                        case "micro_current":
                                curOut = message.get("motor");
                                out = (voltMot1 + voltMot2)/2 * curOut;
				in = voltBat * curIn;
				filter.addPoint(in - out);
				graph.addPoint(0, in - out);
				graph.addPoint(1, filter.filteredPoint());
				break;
			default:
				break;
		}
	}
}