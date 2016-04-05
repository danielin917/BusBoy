package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.util.graphs.*;
import solarcar.gui.TelemetryGUI;
import solarcar.util.filters.ExponentialFilter;

public class  MessagesPerSecondGraph extends SolarPanel implements DataMessageSubscriber, Runnable {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    int telems;
    Thread thread;
    SolarFilter filter;
    String[] keys;
    String title;
    int vSize;

    public MessagesPerSecondGraph(String t, String[] k, int v) {
        super();
        title = t;
        vSize = v;
        keys = new String[k.length];
        System.arraycopy(k, 0, keys, 0, keys.length);
    }

    @Override
    public void init() {
        setBorder(new TitledBorder(this.title));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>m<br/>s<br/>g<br/>/<br/>s");
        graph = new MultiGraphPanel(60, 20, 20, vSize, 0, 2);
        graph.setColor(0, Color.LIGHT_GRAY);
        graph.setColor(1, Color.BLUE);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
        graph.setNumLines(10);

        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.2)};
        filter.setParams(args);

        telems = 0;
        thread = new Thread(this, "MessagesPerSecondGraph");
        thread.start();

        for (String key : keys) {
            MessageHandler.get().subscribeData(key, this);
        }
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        /*
         * for(int x = 0; x < keys.length; x++) {
			if(keys[x].equals(message.getId()))
         */
        telems++;
//		}
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            graph.addPoint(0, ((double) telems * 10));
            filter.addPoint(((double) telems * 10));
            graph.addPoint(1, filter.filteredPoint());
            telems = 0;
        }
    }
}