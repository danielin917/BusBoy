package solarcar.gui.modules;


import java.awt.*;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.TelemetryGUI;
import solarcar.vdcListener.VDCConn;

public class  SegmentIdentifier extends SolarPanel implements Runnable {

    private JLabel segmentLabel;
    private JLabel segLen;
    private JLabel endCond;
    private JLabel grade;
    private JLabel speedlimit;
    private JLabel passing;
    private Thread thread;
    private int segment;
    private int columnCount;
    //private Vector columns, data, row;
    private ArrayList route_data = new ArrayList(), row;
    private static final double Earth_radius = 6371.0;
    private int updateFreq;
    private Boolean data_status = false, route_status = false;
    private String checked_rows;
    private int column;
    private double latitude, longitude, elevation;
    private ArrayList route_lat, route_lon, route_grade, route_speedlimit, route_passing, route_startdis, route_seglen, route_segid;
    private ArrayList route_hazard;
    private ArrayList route_startLat, route_endLat, route_startLong, route_endLong;
    private String hazard_string;
    private int smallest_idx, pass_idx;
    private int second_smallest_idx;
	private int segID, prevSegID;
    Integer smallest_segid;
    private DecimalFormat df;
    //private int fail = 0;

    @Override
    public void init() {
        setBorder(new TitledBorder("Segment Identifier"));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(350, 112));

        segmentLabel = new JLabel("N/A");
        segLen = new JLabel("Segment Length: N/A mi");
        endCond = new JLabel("End Condition: N/A ");
        grade = new JLabel("Grade: N/A %");
        speedlimit = new JLabel("Speed Limit: N/A MPH");
        passing = new JLabel("Passing in: N/A mi");

        segmentLabel.setFont(new Font(segmentLabel.getFont().getFontName(),
                segmentLabel.getFont().getStyle(), 30));

        updateFreq = 1;

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 4;
        c.ipadx = 10;
        c.ipady = 3;
        c.anchor = GridBagConstraints.CENTER;
        add(segmentLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_START;
        add(segLen, c);

        c.gridx = 1;
        c.gridy = 2;
        //c.gridwidth = 1;	c.gridheight = 1;
        //c.anchor = GridBagConstraints.LINE_START;
        add(endCond, c);


        c.gridx = 1;
        c.gridy = 1;
        //c.gridwidth = 1;	c.gridheight = 1;
        //c.anchor = GridBagConstraints.LINE_START;
        add(passing, c);


        c.gridx = 1;
        c.gridy = 3;
        //c.gridwidth = 1;	c.gridheight = 1;
        //c.anchor = GridBagConstraints.LINE_START;
        add(grade, c);

        c.gridx = 1;
        c.gridy = 4;
        //c.gridwidth = 1;	c.gridheight = 1;
        //c.anchor = GridBagConstraints.LINE_START;
        add(speedlimit, c);

        /*
         * c.gridx = 2;	c.gridy = 0; //c.gridwidth = 1;	c.gridheight = 1;
         * //c.anchor = GridBagConstraints.LINE_START; add(passing, c);
         */

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);



        thread = new Thread(this, "SegmentIdentifier");
        thread.start();
    }

	private long sqlTimeout = 0;
    
    private void updateData() {
        segmentLabel.setText(Integer.toString(smallest_segid));
        segmentLabel.setForeground(Color.BLACK);
        speedlimit.setText("Speed Limit: " + Double.toString((Double) route_speedlimit.get(smallest_idx)) + " MPH");
        endCond.setText("End Condition: " + hazard_string);

        if (pass_idx != smallest_idx && pass_idx != route_passing.size() - 1) {
            //double pass_dis = (((Double) route_startdis.get(pass_idx)) - ((Double) route_startdis.get(smallest_idx)));
            //passing.setText("Passing in: " + df.format(pass_dis) + " km");
        } else if (pass_idx == smallest_idx & (Boolean) route_passing.get(pass_idx) == false) {
            passing.setText("Passing in: NOW");
            passing.setForeground(Color.ORANGE);

        } else {
            passing.setText("Passing in: NO MORE");
            passing.setForeground(Color.RED);
        }

        if (smallest_idx != route_seglen.size() - 1) {
            segLen.setText("Segment Length: " + df.format((Double) route_seglen.get(smallest_idx)) + " km");
        } else {
            segLen.setText("Segment Length: FINISH NEXT");
            segLen.setForeground(Color.RED);
        }

        grade.setText("Grade: " + (df.format((Double) route_grade.get(smallest_idx))) + " %");
        if ((Double) route_grade.get(smallest_idx) >= 1.5) {
            grade.setForeground(Color.RED);
        } else if ((Double) route_grade.get(smallest_idx) < -1.5) {
            grade.setForeground(Color.GREEN);
        } else {
            grade.setForeground(Color.BLACK);
        }

        //endCon.setText(Double.toString(route_endcon));
        //hazardLen.setText(Double.toString(route_hazardlen));
        //grade.setText(Double.toString(route_grade));



    }

    private void acquireCurrentSegment() {
        //System.out.println("attempting to get gps data(segID)");
        if (TelemetryGUI.getSQLConn() != null) {

            // insert timeout here
            String query = "SELECT * FROM  gps.localcartable ORDER BY recordedtime DESC LIMIT 1";
            Statement st = null;
            try {
                st = TelemetryGUI.getSQLConn().createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.last()) {
					segID = rs.getInt("segId");
                }

				segmentLabel.setText(segID + "");
				
				if(prevSegID != segID) {
				//	VDCConn.get().sendMessage("chat msg='" + segID + " ' id='Seg ID:'");
					prevSegID = segID;
				}
				
                /*
                 * latitude = -34.72; longitude = 139.58; elevation = 100;
                 */
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
        } else {
            System.out.println("no connection; not attempting(GPS(segID))");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ex) {
				Logger.getLogger(SegmentIdentifier.class.getName()).log(Level.SEVERE, null, ex);
			}
        }

    }

    private void nextPointInfo() {
        //Boolean notPassing = true;
        pass_idx = smallest_idx;
        while ((Boolean) route_passing.get(pass_idx) != false && pass_idx < route_passing.size() - 1) {
            pass_idx = pass_idx + 1;
        }
    }

    //TODO-switch case method
    private void hazardManager() {
        if ((Integer) route_hazard.get(smallest_idx) == 1) {
            hazard_string = "End of Race";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 2) {
            hazard_string = "Cowgate";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 3) {
            hazard_string = "Speed Limit Change";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 4) {
            hazard_string = "Stop Sign";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 5) {
            hazard_string = "Traffic Light";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 6) {
            hazard_string = "Yield/Blinking Yellow";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 7) {
            hazard_string = "Railroad";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 8) {
            hazard_string = "Passing Zone Change";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 9) {
            hazard_string = "Right Turn";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 10) {
            hazard_string = "Left Turn";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 11) {
            hazard_string = "Straight";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 12) {
            hazard_string = "Fork Right";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 13) {
            hazard_string = "Fork Left";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 14) {
            hazard_string = "Other/POI";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 15) {
            hazard_string = "Short Ctrl Stop";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 16) {
            hazard_string = "Long Ctrl Stop";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 17) {
            hazard_string = "Stage Stop";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 19) {
            hazard_string = "Nothing";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 20) {
            hazard_string = "Max Length";
            endCond.setForeground(Color.BLACK);
        } else if ((Integer) route_hazard.get(smallest_idx) == 21) {
            hazard_string = "Max Curvature";
            endCond.setForeground(Color.RED);
        } else if ((Integer) route_hazard.get(smallest_idx) == 22) {
            hazard_string = "Constant Grade";
            endCond.setForeground(Color.BLACK);
        } else {
            hazard_string = "Unknown";
            endCond.setForeground(Color.ORANGE);
        }
    }

    private void resetNA() {
        segmentLabel.setText("Off Route");
        segmentLabel.setForeground(Color.RED);
        segLen.setText("Segment Length: N/A mi");
        endCond.setText("End Condition: N/A ");
        grade.setText("Grade: N/A %");
        speedlimit.setText("Speed Limit: N/A MPH");
        passing.setText("Passing in: N/A mi");
    }

    @Override
    public void run() {

		while (true) {

            acquireCurrentSegment();
            /*if (smallest_segid != null) {
                nextPointInfo();
                hazardManager();
                updateData();
            } else {
                resetNA();
            }
            try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
            }*/
        }
    }
}
