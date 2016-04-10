package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.graphs.GraphPanelGrid;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;

public class  BatTempGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    GraphPanelGrid graph;
    long previousTimeStamp;
    

    @Override
    public void init() {
        setBorder(new TitledBorder("Batt Temp Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>T<br/>e<br/>m<br/>p</html>");
        graph = new GraphPanelGrid(1 * 60 * 60, 20, 1, 60, 20);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        MessageHandler.get().subscribeData("bmstempextremes", this);
        previousTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (System.currentTimeMillis() -1000 > previousTimeStamp)
        {
            previousTimeStamp = System.currentTimeMillis();
            double  avgTemp = (message.get("min") + message.get("max"))/2;

            graph.addPoint(avgTemp);
        }
        //graph.addPoint(2.5);
    }
}
