package solarcar.gui.modules;

import solarcar.util.graphs.MultiGraphPanel;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.util.filters.SolarFilter;
import solarcar.vdcListener.MessageHandler;
import solarcar.util.filters.SolarFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import solarcar.gui.tabs.OptionsTab;
import solarcar.util.filters.ExponentialFilter;
import solarcar.util.filters.averageStream;
import solarcar.util.graphs.*;

public class  WindSpeedGraph extends SolarPanel implements Runnable {
    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    SolarFilter filter;
    Thread thread;
    averageStream windSpeedStreamReal;
    averageStream windSpeedStreamSimulated;
    private int updateFreq;
    private double predictedWindSpeed;
    private double actualWindSpeed;

    public WindSpeedGraph(int duration, int upperBound) {
        super();                                        
        setBorder(new TitledBorder("Wind Speed Graph"));
        windSpeedStreamReal=new averageStream(3600*20);
        windSpeedStreamSimulated = new averageStream(3600*20);
        setPreferredSize(new Dimension(195, 115));
        graph = new MultiGraphPanel(duration, 1, 1, upperBound, 0, 3);
        
	graph.start();
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
       // graph.setColor(1, Color.BLUE);
        graph.setColor(1, Color.RED);
        graph.setColor(2, new Color(0xAA00AA));
        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);
        
        (thread = new Thread(new WindSpeedGraph.queryThread())).start();

        taxis.setHorizontalAlignment(JLabel.CENTER);

        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.005)};
        filter.setParams(args);
        updateFreq = 1;
    }
    
    @Override
    public void init() {
        thread = new Thread(this, "Wind Speed Graph");
        thread.start();
    }

    private void collectActual() {
        String query = "SELECT * FROM " + TelemetryGUI.db + ".wxdata ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
//				System.out.println(rs.getDouble("solarRadiation"));
                actualWindSpeed = rs.getDouble("windSpeedCorr");
                if (OptionsTab.get().opt_mph()) {
                    actualWindSpeed = actualWindSpeed * 3600 / 1609.344;
                } else if (OptionsTab.get().opt_kph()) {
                    actualWindSpeed = actualWindSpeed * 3600 / 1000;
                }
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

    @Override
    public void run() {
        while (true) {
            if (TelemetryGUI.getSQLConn() != null) {
                collectActual();
                query();
            }
            graph.addPoint(0, actualWindSpeed);
            graph.addPoint(1, predictedWindSpeed);
            filter.addPoint(actualWindSpeed);
            //graph.addPoint(1, filter.filteredPoint());
            //graph.addPoint(2,predictedWindSpeed);
            windSpeedStreamReal.insert(actualWindSpeed);
            windSpeedStreamSimulated.insert(predictedWindSpeed);
        try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
		System.out.println(rs.getDouble("totalRadiation"));
                predictedWindSpeed = rs.getDouble("totalRadiation");
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
