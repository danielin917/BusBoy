package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.tabs.OptionsTab;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  SpeedGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;

    public SpeedGraph() {
        super();
        setBorder(new TitledBorder("Speed Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>S<br/>p<br/>e<br/>e<br/>d</html>");

        if(OptionsTab.get().opt_mph()) {
            graph = new MultiGraphPanel(600, 5, 20, 80, 0, 2);
        } else if(OptionsTab.get().opt_kph()) {
            graph = new MultiGraphPanel(600, 5, 20, 120, 0, 2);
        }
        
        graph.setColor(1, Color.BLUE);
		graph.start();
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("motvel", this);
        MessageHandler.get().subscribeData("cruise", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "motvel":
                if (OptionsTab.get().opt_mph()) {
                    graph.addPoint(0, message.get("vehvel") * 3600 / 1609.344);
                } else if (OptionsTab.get().opt_kph()) {
                    graph.addPoint(0, message.get("vehvel") * 3600 / 1000);
                } else if (OptionsTab.get().opt_ms()) {
                    graph.addPoint(0, message.get("vehvel"));
                }
                break;
            case "cruise":
                if (OptionsTab.get().opt_mph()) {
                    graph.addPoint(1, message.get("speed") * 3600 / 1609.344);
                } else if (OptionsTab.get().opt_kph()) {
                    graph.addPoint(1, message.get("speed") * 3600 / 1000);
                } else if (OptionsTab.get().opt_ms()) {
                    graph.addPoint(1, message.get("speed"));
                }
                break;
			default:
				break;
        }
    }
}