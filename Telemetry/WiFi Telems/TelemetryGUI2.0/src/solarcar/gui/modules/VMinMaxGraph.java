package solarcar.gui.modules;


import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.vdcListener.MessageHandler;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class VMinMaxGraph extends SolarPanel implements DataMessageSubscriber {
	double min0=0,min1=0,max0=0,max1=0,min,max;
    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    int time = 60;

    public VMinMaxGraph(int time) {
        super();
        this.time = time;
    }

    public VMinMaxGraph() {
        super();
        this.time = 300;
    }

    @Override
    public void init() {
        setBorder(new TitledBorder("Vmin/max Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>V<br/>o<br/>l<br/>t<br/>s</html>");
        System.out.println("about to create new graph");
        graph = new MultiGraphPanel(time, 20, 20, 5, 0, 2);
		graph.setColor(0, Color.blue);
		graph.setColor(1, Color.pink);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        MessageHandler.get().subscribeData("micro_current", this);
        // MessageHandler.get().subscribeData("bmsvoltextremes_aux", this);

    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        
        // switch (message.getId()) {
            // case "bmsvoltextremes":
                // min0 = message.get("min");
                // max0 = message.get("max");
            // case "bmsvoltextremes_aux":
                // min1 = message.get("min");
                // max1 = message.get("max");
        // }
             
        // min = Math.min(min0,min1);
        // max = Math.max(max0,max1);
        graph.addPoint(0, message.get("micro"));
        // graph.addPoint(0, max);
        // graph.addPoint(1, min);
        //graph.addPoint(2.5)
    }

    /*
     * public void setParams(String args[]) { graph = null; time =
     * Integer.parseInt(args[0]); //graph = new MultiGraphPanel(time, 100, 20,
     * 4.2, 2.0, 2); System.out.println("set time to: " + time);
	}
     */
}
