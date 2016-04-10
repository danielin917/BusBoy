package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.util.graphs.Trace2DPanelGrid;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class wsMotVoltVecGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel xaxis;
    JLabel yaxis;
    Trace2DPanelGrid graph;

    public wsMotVoltVecGraph() {
        super();
        setBorder(new TitledBorder("Volt Vector"));
        setPreferredSize(TelemetryGUI.HalfGraph);
        setLayout(new BorderLayout());
        xaxis = new JLabel("Imag");
        yaxis = new JLabel("<html>R<br/>e<br/>a<br/>l</html>");

        graph = new Trace2DPanelGrid(30, 5, 5, 60, -5, 10, -10);
		graph.start();
        //graph = new Trace2DPanelGrid(6)
        graph.setColor(Color.BLUE);
        add(xaxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        xaxis.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("motvoltvector", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (message.getId().equals("motvoltvec")) {
            graph.addPoint(message.get("vq"), message.get("real"));
            //System.out.println("img: " + message.get("img") + " real: " + message.get("real"));
        }
    }
}