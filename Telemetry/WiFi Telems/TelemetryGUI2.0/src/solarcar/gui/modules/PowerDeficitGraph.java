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

public class  PowerDeficitGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    private double curIn;
    private double curOut;
    private double volt;
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
        volt = 0;
        in = 0;
        out = 0;

        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.005)};
        filter.setParams(args);

        MessageHandler.get().subscribeData("cur0", this);
        MessageHandler.get().subscribeData("motbus", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "cur0":
                curIn = message.get("array");
                break;
            case "motbus":
                volt = message.get("volt");
                curOut = message.get("curr");
                out = volt * curOut;
                in = volt * curIn;
                filter.addPoint(in - out);
                graph.addPoint(0, in - out);
                graph.addPoint(1, filter.filteredPoint());
                break;
        }
    }
}