package solarcar.gui.modules;


import solarcar.gui.tabs.OptionsTab;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import solarcar.gui.TelemetryGUI;

public class AnemometerPanel extends JPanel implements Runnable, HierarchyListener {

    private int updateFreq;
    private Thread thread;
    private int xoffset;
    private int yoffset;
    private double windAngle, windSpeed, gustSpeed;
    private double predAngle, predSpeed;
    private double radiation;
    //private double airAngle;
    //private double airSpeed;
    private double maxSpeed;
    private int yCenterOff;
    private JLabel textLabel;
    private DecimalFormat df;
    private double blueAngle, blueSpeed;
    private BufferedImage car_image;

    public AnemometerPanel(JLabel textLabel) {
        setLayout(null);

        this.updateFreq = 3;

        xoffset = 1;
        yoffset = 1;

        //airAngle = 0;
        //airSpeed = 0;
        windAngle = 0;
        windSpeed = 0;
        blueAngle = 0;
        blueSpeed = 0;
        //maxSpeed = 13.4;
        maxSpeed = 15;
        predAngle = 0;
        predSpeed = 0;
        radiation = 0;
        gustSpeed = 0;

        //	String os = System.getProperty("os.name").toLowerCase();
        //	if(os.indexOf("win") >= 0)
        //		yCenterOff = 0;
        //	else 
        yCenterOff = 1;

        this.textLabel = textLabel;
        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

	@Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            //System.out.println("Component shown");
            synchronized (this) {
                notifyAll();
            }
        }
    }

	@Override
    public void paintComponent(Graphics g) {
        double speed = windSpeed; // (OptionsTab.get().useWindSpeed()) ? windSpeed : windSpeed;
        double angle = windAngle; // (OptionsTab.get().useWindDir()) ? windAngle : windAngle; This is the RELATIVE wind angle
        maxSpeed = (OptionsTab.get().useWindSpeed()) ? 15 : 45;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D g3 = (Graphics2D) g;
        Dimension size = this.getSize();
        if (size.height > size.width) {
            yoffset = size.height - size.width + 1;
        } else if (size.width > size.height) {
            xoffset = size.width - size.height + 1;
        }
        size.height = size.height - yoffset;
        size.width = size.width - xoffset;

        int centerx = size.width / 2;
        int centery = size.height / 2;
        //angle = (angle + 360) % 360;
        double z = centerx * speed / maxSpeed;
        double xcomp = z * Math.sin(Math.toRadians(angle));
        double ycomp = z * Math.cos(Math.toRadians(angle));
        //set quantum car
//        try {
//            car_image = ImageIO.read(new File("resources/quantum-small.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


         g2.drawImage(car_image, centerx - 32, centery - 90, null);  //manual moving, TODO - fix dat
         g2.setColor(Color.BLACK);
         //GradientPaint gp = new GradientPaint(75,75,Color.BLUE,95,95,Color.YELLOW);
         //g2.setPaint(gp);
         g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g2.drawLine(centerx, centery + yCenterOff, (int) (xcomp + centerx + 0.5), (int) (size.height - (ycomp + centery) + 0.5));
         
         g2.setColor(Color.RED);
         z = centerx * predSpeed / maxSpeed;
         xcomp = z*Math.sin(Math.toRadians(predAngle));
         ycomp = z*Math.cos(Math.toRadians(predAngle));
         g2.drawLine(centerx, centery+yCenterOff, (int)(xcomp + centerx + 0.5), (int)(size.height - (ycomp + centery) + 0.5));
         
         g2.setColor(Color.BLUE);
         
         z = centerx * blueSpeed / maxSpeed+20;
         xcomp = centerx*Math.sin(Math.toRadians(blueAngle));
         ycomp = centerx*Math.cos(Math.toRadians(blueAngle));
         g2.drawLine(centerx, centery+yCenterOff, (int)(xcomp + centerx + 0.5), (int)(size.height - (ycomp + centery) + 0.5));
            
         g2.setColor(Color.BLACK);
            
         
         //draw circles and lines
         g2.setStroke(new BasicStroke(1));
         g2.drawOval(0, 0, size.width, size.height);
         z = centerx / 3;
         g2.drawOval((int) (centerx - z), (int) (centery - z), (int) (2 * z), (int) (2 * z));
         z = centerx * 2 / 3;
         g2.drawOval((int) (centerx - z), (int) (centery - z), (int) (2 * z), (int) (2 * z));
         g2.drawLine(centerx, centery+yCenterOff, centerx, 0);

    }

    private void collectData() {
        //Connection conn = TelemetryGUI.getSQLConn();
        String query = "SELECT * FROM " + TelemetryGUI.db + ".wxdata ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                //airAngle = rs.getDouble("airHeading");
                windAngle = rs.getDouble("windDirCorr");
                windSpeed = rs.getDouble("windSpeedCorr");
                gustSpeed = rs.getDouble("gustSpeedCorr");
               
                if (OptionsTab.get().opt_mph()) {
                    windSpeed = windSpeed * 3600 / 1609.344;
                    gustSpeed = gustSpeed *3600 / 1609.344;
                } else if (OptionsTab.get().opt_kph()) {
                    windSpeed = windSpeed * 3600 / 1000;
                    gustSpeed = gustSpeed * 3600/1000;
                }
                radiation = rs.getDouble("radiation");

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

        st = null;
        query = "SELECT * FROM weather.wxdata ORDER BY recordedtime DESC LIMIT 1";
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                blueAngle = rs.getDouble("carHeading");
                if (OptionsTab.get().opt_mph()) {
                    blueSpeed = rs.getDouble("carSpeed") * 3600 / 1609.344;
                } else if (OptionsTab.get().opt_kph()) {
                    blueSpeed = rs.getDouble("carSpeed") * 3600 / 1000;
                } else if (OptionsTab.get().opt_ms()) {
                    blueSpeed = rs.getDouble("carSpeed");
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
        
        st = null;
        query = "SELECT * FROM simulator.dlsd order by segmentTime";
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
                predAngle = rs.getDouble("windDirection");
                if (OptionsTab.get().opt_mph()) {
                    predSpeed = rs.getDouble("windSpeed") * 3600 / 1609.344;
                } else if (OptionsTab.get().opt_kph()) {
                    predSpeed = rs.getDouble("windSpeed") * 3600 / 1000;
                } else if (OptionsTab.get().opt_ms()) {
                    predSpeed = rs.getDouble("windSpeed");
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

        /*
         * windAngle = windAngle + 1; windSpeed = 10;
         *
         * airAngle = airAngle - 1; airSpeed = 5;
         */
        //windAngle = (windAngle + 360) % 360;
        //airAngle = (airAngle + 360) % 360;
    }

    private void updateText() {
        if (OptionsTab.get().opt_mph()) {
            textLabel.setText("<html>Wind: " + df.format(windSpeed) + " mph " + df.format(windAngle)
                    + "&deg;<br>" + "Pred: "	+ df.format(predSpeed)  + " mph " + df.format(predAngle)  + "&deg;<br>" + 
                    "Car: " + df.format(blueSpeed) + " mph " + df.format(blueAngle) + "&deg;<br>" + "Rad: " + 
                    df.format(radiation)+ "W/m^2 <br> Gust Speed: " + df.format(gustSpeed) + " mph</html>");
        } else if (OptionsTab.get().opt_kph()) {
            textLabel.setText("<html>Wind: " + df.format(windSpeed) + " kph " + df.format(windAngle)
                    + "&deg;<br>" + "Pred: "	+ df.format(predSpeed)  + " kph " + df.format(predAngle)  + "&deg;<br>" + 
                    "Car: " + df.format(blueSpeed) + " kph " + df.format(blueAngle) + "&deg;<br>" + 
                    "Rad: " + df.format(radiation)+ "W/m^2 <br> Gust Speed: " + df.format(gustSpeed) + " kph</html>");
        } else if (OptionsTab.get().opt_ms()) {
            textLabel.setText("<html>Wind: " + df.format(windSpeed) + " m/s " + df.format(windAngle) + "&deg;<br>"
                    + "&deg;<br>" + "Pred: "	+ df.format(predSpeed)  + " m/s " + df.format(predAngle)  + "&deg;<br>" + 
                    "Car: " + df.format(blueSpeed) + " m/s " + df.format(blueAngle) + "&deg;</html>" + 
                    "Rad: " + df.format(radiation)+ "W/m^2 <br> Gust Speed: " + df.format(gustSpeed) + " m/s</html>");
        }
    }

	public void start() {
        thread = new Thread(this, "Anemometer Thread");
        thread.start();
	}
	
	@Override
	@SuppressWarnings("SleepWhileInLoop")
    public void run() {
		this.addHierarchyListener(this);
        while (true) {
            synchronized (this) {
                while (!this.isShowing()) {
                    try {
                        //System.out.println("waiting for graph to be visible");
                        wait();
                    } catch (Exception e) {
                        //
                    }
                }
            }

            if (TelemetryGUI.getSQLConn() != null) {
                this.collectData();
            }
            this.repaint();
            this.updateText();
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
            }
        }
    }
}