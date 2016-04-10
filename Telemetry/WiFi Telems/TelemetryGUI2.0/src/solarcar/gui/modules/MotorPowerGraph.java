package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.filters.averageStream;
import solarcar.util.graphs.*;

public class  MotorPowerGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    SolarFilter filter;
    Thread thread;
    averageStream poutStreamReal;
    averageStream poutStreamSimulated;
    private double predictedPowerOut;
    private double voltMot1, voltMot2;
    private double cur;

    public MotorPowerGraph(String size,int duration, int upperBound) {
        super();                                        
        setBorder(new TitledBorder("Motor Power Graph"));
        poutStreamReal=new averageStream(3600*20);
        poutStreamSimulated = new averageStream(3600*20);
        if (size.equals("bigger")) {
            setPreferredSize(TelemetryGUI.HalfGraph);
            graph = new MultiGraphPanel(duration, 20, 5, upperBound, 0, 4);
            graph.setNumLines(10);
        } else if(size.equals("Square")){
            setPreferredSize(TelemetryGUI.QuarterGraph);
            graph = new MultiGraphPanel(duration, 20, 5, upperBound, 0, 4);
            graph.setNumLines(5);
        } else {
            setPreferredSize(TelemetryGUI.WideGraph);
            graph = new MultiGraphPanel(duration, 20, 5, upperBound, 0, 4);
            graph.setNumLines(5);
        }
		graph.start();
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
        graph.setColor(1, Color.BLUE);
        graph.setColor(2, Color.RED);
        graph.setColor(3, new Color(0xAA00AA));
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);
        
        (thread = new Thread(new queryThread())).start();

        taxis.setHorizontalAlignment(JLabel.CENTER);

        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.005)};
        filter.setParams(args);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("motbus", this);
        MessageHandler.get().subscribeData("motbus2", this);
        MessageHandler.get().subscribeData("micro_current",this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        if(msg.getId().equals("motbus")) 
        {
             voltMot1 = msg.get("volt");
        }
        else if(msg.getId().equals("motbus2"))
        {
            voltMot2 = msg.get("volt");
        }
        else if (msg.getId().equals("micro_current"))
        {
            cur = msg.get("motor");
            graph.addPoint(0, cur * (voltMot1 + voltMot2)/2);
            filter.addPoint(cur * (voltMot1 + voltMot2)/2);
            graph.addPoint(1, filter.filteredPoint());
            graph.addPoint(2,predictedPowerOut);
            poutStreamReal.insert(cur*(voltMot1 + voltMot2)/2);
            poutStreamSimulated.insert(predictedPowerOut);
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
		System.out.println(rs.getDouble("powerOut"));
                predictedPowerOut = rs.getDouble("powerOut");
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
