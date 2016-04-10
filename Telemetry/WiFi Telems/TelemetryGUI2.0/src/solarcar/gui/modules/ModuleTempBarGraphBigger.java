package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.util.graphs.*;
import solarcar.gui.TelemetryGUI;

public class  ModuleTempBarGraphBigger extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    BarGraphPanel graph;
    private static final int NUM_MODULE_MESSAGES = 11;


    public ModuleTempBarGraphBigger() {
        super();
        setBorder(new TitledBorder("Module Temperatures"));
        setPreferredSize(TelemetryGUI.HalfGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Modules");
        yaxis = new JLabel("<html>T<br/>e<br/>m<br/>p</html>");
        graph = new BarGraphPanel(20, 75, 0, 5, 45, 60);

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void init() {
        for (int i = 0; i < NUM_MODULE_MESSAGES; i++) {
            MessageHandler.get().subscribeData("bms_temp_" + i, this);
        }
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        //System.out.println("adding point to graph");
        int messageNum = Integer.parseInt(message.getId().substring(9));

        for (int j = 0; j < 4 && messageNum*4+j < 43; j++){
            int mod = messageNum*4 + j;
            graph.addPoint(mod, message.get("temp_" + mod));
        }
    }
}