package solarcar.gui.modules;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;

public class WeatherOverviewModule extends SolarPanel implements Runnable {

    private JLabel actualRaidiationName, actualRaidiationVal;
    private JLabel predictedRadiationName, predictedRadiationVal;
    private JLabel tempName, tempVal;
    private JLabel airDensityName, airDensityVal;
    private Thread thread;
    private int updateFreq;
    private double current_radiation, predicted_radiation;
    private DecimalFormat df;
    private long sqlTimeout;
    private long time, curTime;

    public double getCurrentRadiation() {
        return current_radiation;
        //return predictedRadiationVal;
    }

    @Override
    public void init() {
        // initialize panel
        setBorder(new TitledBorder("Weather Overview"));
        setPreferredSize(new Dimension(625, 65));		//625
        setLayout(new GridLayout(0, 4, 5, 0));

        // Initialize value labels
        actualRaidiationVal = new JLabel("N/A");
        predictedRadiationVal = new JLabel("N/A");
        tempVal = new JLabel("N/A");
        airDensityVal = new JLabel("N/A");

        // Initialize name labels
        actualRaidiationName = new JLabel("Actual Radiation: ");
        predictedRadiationName = new JLabel("Predicted Radiation: ");
        tempName = new JLabel("Temp: ");
        airDensityName = new JLabel("Air Pressure: ");

        // Add labels
        add(predictedRadiationName);
        add(predictedRadiationVal);
        add(airDensityName);
        add(airDensityVal);
        add(actualRaidiationName);
        add(actualRaidiationVal);
        add(tempName);
        add(tempVal);

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        updateFreq = 1;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            if (TelemetryGUI.getSQLConn() != null) {
//				/updateFreq = 1;
                //System.out.println("attempting to get wx data");
                // insert timeout here
               // int i=graph.getLineLength(1);
               /// graph.resetWriteHead(1);
               // while (rs.next() && i>=0) {
               //     double powerIn = rs.getDouble("horizontalRadiation");
               //     long time=rs.getTimestamp("segmentTime").getTime();
               //     System.out.println(rs.getTimestamp("segmentTime").getTime());
               //     graph.addPointFromWrite(1,powerIn,time);
               //     --i;
               // }
                Statement st = null;
                try {
                    st = TelemetryGUI.getSQLConn().createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM simulator.dlsd ORDER BY segmentTime DESC LIMIT 1;");
                    if (rs.last()) {
                        predicted_radiation = rs.getDouble("horizontalRadiation");
                        predictedRadiationVal.setText("<html>" + df.format(rs.getDouble("horizontalRadiation")) + " w/m<sup>2</sup></html>");
                    }
            
                    rs = st.executeQuery("SELECT * FROM " + TelemetryGUI.db + ".pyranometer ORDER BY recordedtime DESC LIMIT 1");
                    if (rs.last()) {
                        current_radiation = rs.getDouble("solarRadiation");
                        actualRaidiationVal.setText("<html>" + df.format(rs.getDouble("solarRadiation")) + " w/m<sup>2</sup></html>");
                    }

                    rs = st.executeQuery("SELECT * FROM " + TelemetryGUI.db + ".anemometer ORDER BY recordedtime DESC LIMIT 1");
                    if (rs.last()) {
                        tempVal.setText("<html>" + df.format((rs.getDouble("airTemp") - 32) * 5 / 9) + "&deg;C</html>");
                        airDensityVal.setText("<html>" + df.format(rs.getDouble("pressure")) + " hPa</html>");
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
                //airDensityName.setText("db conn");
				sqlTimeout = 0;
            } else {
                System.out.println("no connection; not attempting(update wx)");
				if(sqlTimeout == 0)
					sqlTimeout = System.currentTimeMillis();
				// if we've been failing the past minute, give up for about 10 minutes
				else if( (System.currentTimeMillis() - sqlTimeout) > 60000 ) 
				{
					try {
						Thread.sleep(600*1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
                //airDensityName.setText("no db conn");
                //updateFreq = 1/60;
            }

            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
