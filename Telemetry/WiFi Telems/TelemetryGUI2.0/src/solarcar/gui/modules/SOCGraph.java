package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.tabs.OptionsTab;
import solarcar.util.filters.averageStream;
import solarcar.util.filters.endpointStream;
import solarcar.util.graphs.GraphPanelGrid;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  SOCGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    Thread thread;
    endpointStream SOCStreamReal;
    endpointStream SOCStreamSimulated;
    private double predictedSOC;
    private double realSOC;

    public SOCGraph(String size, int duration)
    {
        super();
        SOCStreamReal=new endpointStream(3600*20);
        SOCStreamSimulated = new endpointStream(3600*20);
        setBorder(new TitledBorder("SOC Graph"));
        if(size.equals("Small"))
            setPreferredSize(TelemetryGUI.QuarterGraph);
        else
            setPreferredSize(TelemetryGUI.HalfGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("%");
        graph = new MultiGraphPanel(duration, 20, 20, 100, 0, 4);
        graph.setColor(1,Color.red);
        graph.setColor(2,Color.MAGENTA);
        graph.setColor(3, new Color(0xAA00AA));

        
        //graph = new GraphPanelGrid(60*20, 20, 20, 100, 0);
        graph.start();


        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        (thread = new Thread(new queryThread())).start();
        
        taxis.setHorizontalAlignment(JLabel.CENTER);
        graph.setNumLines(10);

    }

    public void init() {
        MessageHandler.get().subscribeData("SOC", this);
        MessageHandler.get().subscribeData("SOCDebug1", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if(message.getId().equals("SOC")) {
            realSOC = message.get("SOC") + OptionsTab.get().SOCOffset();
            graph.addPoint(0,realSOC);
            graph.addPoint(1,predictedSOC);
            SOCStreamReal.insert(realSOC);
            SOCStreamSimulated.insert(predictedSOC);
        }
        else if (message.getId().equals("SOCDebug1")){
            graph.addPoint(2, message.get("current")+50);            
        }
    }
    public void query()
    {
        String query = "SELECT * FROM simulator.dlsd ORDER BY segmentTime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                predictedSOC = rs.getDouble("SOC");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public class queryThread implements Runnable {
        public void run()
        {
            while (true) {
                if (TelemetryGUI.getSQLConn() != null) {
                    query();
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}