package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.util.graphs.*;
import solarcar.gui.TelemetryGUI;

public class  ModuleVoltageBarGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    BarGraphPanel graph;
    private static final int NUM_MODULE_MESSAGES = 11;


    public ModuleVoltageBarGraph(Dimension size) {
        super();
        setBorder(new TitledBorder("Module Voltages"));
        setPreferredSize(size);// default: half graph
        setLayout(new BorderLayout());
        taxis = new JLabel("Modules");
        yaxis = new JLabel("<html>V<br/>o<br/>l<br/>t<br/>a<br/>g<br/>e</html>");
        graph = new BarGraphPanel(43, 5, 0, 20, 2.5, 4.2);

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void init() {
        for (int i = 0; i < NUM_MODULE_MESSAGES; i++) {
            MessageHandler.get().subscribeData("bms_volts_" + i, this);
        }
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        int messageNum = Integer.parseInt(message.getId().substring(10));

        for (int j = 0; j < 4 && messageNum*4+j < 43; j++){
            int mod = messageNum*4 + j;
            graph.addPoint(mod, message.get("volt_" + mod));
        }
    }
}