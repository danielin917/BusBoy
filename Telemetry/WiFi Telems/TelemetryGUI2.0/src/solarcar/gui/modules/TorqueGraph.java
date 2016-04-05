package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;
import solarcar.util.graphs.GraphPanel;
import solarcar.util.graphs.MultiGraphPanel;

public class  TorqueGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;

    public TorqueGraph() {
        super();
        setBorder(new TitledBorder("Torque Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>T<br/>o<br/>r<br/>q<br/>u<br/>e</html>");

        graph = new MultiGraphPanel(60, 20, 20, 65, 0, 4);
		graph.setColor(0, Color.pink);
		graph.setColor(1, Color.green);
        graph.setColor(2, Color.red);
        graph.setColor(3, Color.blue);
        
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);
        graph.start();

        taxis.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("motcmd", this);
        MessageHandler.get().subscribeData("motcmd2", this);

    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        if (msg.getId().equals("motcmd")) {
            if (msg.get("vel") < 0) {
                graph.addPoint(2, msg.get("trq") * 100);
				graph.addPoint(3, 0);
            } else if (msg.get("vel") >= 0) {
                graph.addPoint(3, msg.get("trq") * 100);
				graph.addPoint(2, 0);
            } else {
                graph.addPoint(2, 0);
				graph.addPoint(3, 0);
            }
        } else if (msg.getId().equals("motcmd2")) {
            if (msg.get("vel") < 0) {
                graph.addPoint(0, msg.get("trq") * 100);
				graph.addPoint(1, 0);
            } else if (msg.get("vel") >= 0) {
                graph.addPoint(1, msg.get("trq") * 100);
				graph.addPoint(0, 0);
            } else {
                graph.addPoint(0, 0);
				graph.addPoint(1, 0);
            }
        }
    }
    /*
     * protected synchronized void parseMessage(SolarDataMessage message) {
     * if(message.getId().equals("motcmd")) { if(message.get("vel") < 0)	{
     * graph.addPoint(message.get("trq") * -100); } else if(message.get("vel") >
     * 0)	{ graph.addPoint(message.get("trq") * 100); } else	{
     * graph.addPoint(0); } }
     }
     */
}
