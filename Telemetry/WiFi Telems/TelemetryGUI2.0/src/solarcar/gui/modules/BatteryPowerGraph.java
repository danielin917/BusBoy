package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.tabs.OptionsTab;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  BatteryPowerGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    private double cur;
    private double cur_arr;
    private double volt;
    private double power;
    private SolarFilter filter;

    @Override
    public void init() {
        setBorder(new TitledBorder("Battery Power Graph"));
        setPreferredSize(TelemetryGUI.HalfGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
        graph = new MultiGraphPanel(600, 20, 5, 2000, -2000, 5);
        graph.setNumLines(10);
        graph.setColor(0, Color.PINK);
        graph.setColor(1, Color.BLACK);
		graph.start();
        graph.setColor(2,Color.BLUE);
        graph.setColor(3,Color.CYAN);
        graph.setColor(4,Color.GREEN);
        
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
        cur = 0;
        volt = 0;
        power = 0;

        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.005)};
        filter.setParams(args);

        MessageHandler.get().subscribeData("ab_current", this);
        MessageHandler.get().subscribeData("bms_pack_volts", this);
        MessageHandler.get().subscribeData("motbus", this);
        MessageHandler.get().subscribeData("motbus2", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "ab_current":
                cur = message.get("battery");
                power = volt * cur;
                filter.addPoint(power);
                graph.addPoint(1, power);
                graph.addPoint(2, filter.filteredPoint());
                cur_arr = message.get("array");
                graph.addPoint(0, -cur_arr*volt);
                break;
            case "bms_pack_volts":
                 if(OptionsTab.get().opt_full()) {
                    volt = message.get("pack1");
                }
                else {
                    volt = message.get("pack0");
                }
                break;
            case "motbus":
                graph.addPoint(3, message.get("volt")*message.get("curr"));
                break;
            case "motbus2":
                graph.addPoint(4, message.get("volt")*message.get("curr"));
                break;
        }
    }
}