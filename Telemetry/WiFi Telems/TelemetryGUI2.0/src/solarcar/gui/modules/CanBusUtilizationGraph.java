package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;

public class  CanBusUtilizationGraph extends SolarPanel implements DataMessageSubscriber, Runnable {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    int telems;
    Thread thread;
    SolarFilter filter;

    @Override
    public void init() {
        setBorder(new TitledBorder("Recv Bus Utilization"));
        setPreferredSize(TelemetryGUI.WideGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("%");
        graph = new MultiGraphPanel(120, 20, 20, 50, 0, 2);
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
        thread = new Thread(this, "CanBusUtilizationGraph");
        thread.start();

        MessageHandler.get().subscribeAllData(this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        telems++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            graph.addPoint(0, ((double) telems * 108 / (125000 * 0.1) * 100));
            filter.addPoint(((double) telems * 108 / (125000 * 0.1) * 100));
            graph.addPoint(1, filter.filteredPoint());
            telems = 0;
        }
    }
}//500-520