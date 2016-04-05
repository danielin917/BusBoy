package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.graphs.MultiGraphPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;

public class  BatTempGraphBig extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    private double[] temperatures;
        private static final int NUM_MODULE_MESSAGES = 11;


    @Override
    public void init() {
        setBorder(new TitledBorder("Batt Temp"));
        setPreferredSize(TelemetryGUI.HalfGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>T<br/>e<br/>m<br/>p</html>");
        graph = new MultiGraphPanel(60 * 30, 5, 20, 45, 25, TelemetryGUI.NUM_BATT_MODULES+2);
        graph.setNumLines(20);
		graph.start();
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        temperatures = new double[TelemetryGUI.NUM_BATT_MODULES];
        for (int i = 0; i < TelemetryGUI.NUM_BATT_MODULES; i++) {
            graph.setColor(i, new Color(0.0f,0.0f,0.0f,0.05f));
        }
        for (int i = 0; i < NUM_MODULE_MESSAGES; i++) {
            MessageHandler.get().subscribeData("bms_temp_" + i, this);
        }
        
        graph.setColor(TelemetryGUI.NUM_BATT_MODULES, Color.RED);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        int messageNum = Integer.parseInt(message.getId().substring(9));
        
        for (int j = 0; j < 4 && messageNum*4+j < 43; j++){
            int mod = messageNum*4 + j;
            
            graph.addPoint(mod, message.get("temp_" + mod));
            temperatures[mod] = message.get("temp_" + mod);
        }
        double maxT = -1;
        for (int x = 0; x < TelemetryGUI.NUM_BATT_MODULES; x++) {
            graph.setColor(x, Color.LIGHT_GRAY);
            if (temperatures[x] > maxT) {
                maxT = temperatures[x];
            }
        }
        
        graph.addPoint(TelemetryGUI.NUM_BATT_MODULES, 40);
        //graph.addPoint(2.5);
        graph.setColor(TelemetryGUI.NUM_BATT_MODULES+1, Color.BLUE);
        graph.addPoint(TelemetryGUI.NUM_BATT_MODULES+1, maxT);
    }
}
