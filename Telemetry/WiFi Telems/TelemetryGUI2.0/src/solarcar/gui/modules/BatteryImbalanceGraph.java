package solarcar.gui.modules;


import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.tabs.OptionsTab;
import solarcar.vdcListener.MessageHandler;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class BatteryImbalanceGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    int time = 60;

    public BatteryImbalanceGraph(int time) {
        super();
        this.time = time;
    }

    public BatteryImbalanceGraph() {
        super();
        this.time = 60;
    }

    @Override
    public void init() {
        setBorder(new TitledBorder("Imbalance Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
		yaxis = new JLabel("mV");
        // yaxis = new JLabel("<html>V<br/>o<br/>l<br/>t<br/>s</html>");
        System.out.println("about to create new graph");
        graph = new MultiGraphPanel(time, 20, 20, 100, 0, 3);
		graph.start();
		graph.setColor(0, Color.blue);
		graph.setColor(1, Color.green);

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        MessageHandler.get().subscribeData("bmsvoltextremes", this);
        MessageHandler.get().subscribeData("bmsvoltextremes_aux", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        double min0=0,min1=0,max0=0,max1=0,min,max,bal,bal_aux;
        
        switch (message.getId()) {
            case "bmsvoltextremes":
                min0 = message.get("min");
                max0 = message.get("max");
				bal = message.get("balance");
				graph.addPoint(0, bal*1000);
				break;
            case "bmsvoltextremes_aux":
                min1 = message.get("min");
                max1 = message.get("max");
				bal_aux = message.get("balance");
				graph.addPoint(1, bal_aux*1000);
				break;
        }
             
        // min = Math.min(min0,min1);
        // max = Math.max(max0,max1);
        // graph.addPoint(0, (max-min));
        //graph.addPoint(2.5)
    }

    /*
     * public void setParams(String args[]) { graph = null; time =
     * Integer.parseInt(args[0]); //graph = new MultiGraphPanel(time, 100, 20,
     * 4.2, 2.0, 2); System.out.println("set time to: " + time);
	}
     */
}
