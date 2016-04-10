package solarcar.gui.modules;


import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;

public class  WindDirGraph extends SolarPanel implements Runnable {

    private JLabel taxis;
    private JLabel yaxis;
    private MultiGraphPanel graph;
    private Thread thread;
    private int updateFreq;
    private double predicted;
    private double actual;

    @Override
    public void init() {
        setBorder(new TitledBorder("Wind Direction Graph"));
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel(/*
                 * "<html>V<br/>m<br/>i<br/>n</html>"
                 */"");
        updateFreq = 1;
        graph = new MultiGraphPanel(600, 2, updateFreq, 360, 0, 2);
		graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);

        thread = new Thread(this, "WindDirGraph");
        thread.start();

        graph.setColor(1, new Color(0x000000));
        graph.setColor(0, new Color(0xff0000));
    }

    private void collectActual() {
        String query = "SELECT * FROM " + TelemetryGUI.db + ".anemometer ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                actual = rs.getDouble("windDirection");
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
        String query = "SELECT * FROM " + TelemetryGUI.db + ".inputtable ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
//				System.out.println(rs.getDouble("radiation"));
                predicted = rs.getDouble("windDirection");
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
            graph.addPoint(0, actual);
            graph.addPoint(1, predicted);

            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
