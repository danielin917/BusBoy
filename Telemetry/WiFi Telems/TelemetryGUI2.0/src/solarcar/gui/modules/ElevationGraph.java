package solarcar.gui.modules;


import solarcar.util.graphs.GraphPanelGrid;
import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;

import solarcar.gui.TelemetryGUI;

public class  ElevationGraph extends SolarPanel implements Runnable {

    JLabel taxis;
    JLabel yaxis;
    GraphPanelGrid graph;
    private int updateFreq;
    private double elevation;
    private Thread thread;

    @Override
    public void init() {
        setBorder(new TitledBorder("Elevation Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>E<br/>l<br/>e<br/>v<br/>a<br/>t<br/>i<br/>o<br/>n</html>");
        updateFreq = 1;
        graph = new GraphPanelGrid(600, 1, updateFreq, 200, -200);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        thread = new Thread(this, "ElevationGraph");
        thread.start();
        elevation = 0;
    }

    private void collectElevation() {
        String query = "SELECT * FROM gps.localcartable ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                elevation = rs.getDouble("elevation");
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
                collectElevation();
            }

            graph.addPoint(elevation);
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}