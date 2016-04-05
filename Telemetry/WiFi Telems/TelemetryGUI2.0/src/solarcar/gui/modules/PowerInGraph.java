package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
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
import solarcar.gui.TelemetryGUI;
import solarcar.gui.tabs.OptionsTab;
import solarcar.util.SolarPoint;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.filters.SolarFilter;
import solarcar.util.filters.averageStream;
import solarcar.util.graphs.GraphPanelGrid;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  PowerInGraph extends SolarPanel implements DataMessageSubscriber {

    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    private double cur;
    private double volt;
    SolarFilter filter;

        
    averageStream pinStreamReal;
    averageStream pinStreamSimulated;
    double arrayVoltage;
    double pinFrequency;

    int duration;
    private double predicted;
    Thread thread;
    
    @Override
    public void init() {
        
        pinStreamReal=new averageStream(3600*20);
        pinStreamSimulated = new averageStream(3600*20);
        duration = 600;
        setBorder(new TitledBorder("Power In Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
        graph = new MultiGraphPanel(duration, 20, 20, 2000, 0, 3);
        graph.setColor(0, Color.BLUE);
        graph.setColor(1, Color.red);
        graph.setColor(2, new Color(0xAA00AA));

        graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
        cur = 0;
        volt = 0;
        
        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.005)};
        filter.setParams(args);
        
        (thread=new Thread(new queryThread())).start();

        MessageHandler.get().subscribeData("bms_pack_volts",this);
        MessageHandler.get().subscribeData("ab_current", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (message.getId().equals("ab_current")) {
            cur = message.get("array");
            cur = Math.abs(cur);
        }
        else if (message.getId().equals("bms_pack_volts")) {
            if(OptionsTab.get().opt_full()) {
        		volt = message.get("pack1");
        	}
        	else {
        		volt = message.get("pack0");
        	}  
            graph.addPoint(0,volt * cur);
            filter.addPoint(volt * cur);
            graph.addPoint(1, filter.filteredPoint());
            graph.addPoint(2,predicted);
            pinStreamReal.insert(volt*cur);
            pinStreamSimulated.insert(predicted);
        }

    }
    public void query() {
        String query = "SELECT * FROM simulator.dlsd ORDER BY segmentTime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
		System.out.println(rs.getDouble("powerIn"));
                predicted = rs.getDouble("powerIn");
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

  
    /*public void query()
    {
        try {
                //simulator curve
                ResultSet rs;
                Statement st = TelemetryGUI.getSQLConn().createStatement();
                String query="SELECT * FROM simulator.dlsd fulltable INNER JOIN (SELECT segmentID, MAX(simID) AS MaxSimID FROM simulator.mainsim GROUP BY segmentID) subtable ON fulltable.segmentID=subtable.segmentID AND fulltable.simID=subtable.maxSimID ORDER BY fulltable.segmentID ASC;";
                rs = st.executeQuery(query);
                int i=graph.getLineLength(1);
                graph.resetWriteHead(1);
                while (rs.next() && i>=0) {
                    double powerIn = rs.getDouble("powerIn");
                    long time=rs.getTimestamp("segmentTime").getTime();
                    graph.addPointFromWrite(1,powerIn,time);
                    --i;
                }
                graph.dispWriteSection(1);
            } catch (SQLException ex) {
                Logger.getLogger(PowerInGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
    }*/
    
    public class queryThread implements Runnable {
        @Override
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