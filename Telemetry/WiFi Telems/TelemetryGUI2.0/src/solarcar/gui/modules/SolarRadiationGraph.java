package solarcar.gui.modules;


import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.util.filters.averageStream;
import solarcar.gui.TelemetryGUI;

public class  SolarRadiationGraph extends SolarPanel implements Runnable {

    private JLabel taxis;
    private JLabel yaxis;
    MultiGraphPanel graph;
    private Thread thread;
    public averageStream actualRadiationStream;
    public averageStream predictedRadiationStream;
    private int updateFreq;
    private double predicted;
    private double actual;

    
    @Override
    public void init() {
        setBorder(new TitledBorder("Solar Radiation Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel(/*
                 * "<html>V<br/>m<br/>i<br/>n</html>"
                 */"");
        updateFreq = 1;
        predictedRadiationStream = new averageStream(3600);
        actualRadiationStream = new averageStream(3600);
        graph = new MultiGraphPanel(3600, 1, updateFreq, 1500, 0, 3);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        thread = new Thread(this, "SolarRadiationGraph");
        thread.start();

        graph.setColor(1, new Color(0x000000));
        graph.setColor(0, new Color(0xff0000));
        graph.setColor(2, new Color(0xAA00AA));

    }

    private void collectActual() {
        String query = "SELECT * FROM " + TelemetryGUI.db + ".wxdata ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
//				System.out.println(rs.getDouble("solarRadiation"));
                actual = rs.getDouble("radiation");
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

    private void collectPredicted() {
        String query = "SELECT * FROM simulator.dlsd;";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
//				System.out.println(rs.getDouble("solarRadiation"));
                predicted = rs.getDouble("horizontalRadiation");
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
                collectPredicted();
            }
            graph.addPoint(1, actual);
            graph.addPoint(0, predicted);
            actualRadiationStream.insert(actual);
            predictedRadiationStream.insert(predicted);
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();            }
        }
    }
}