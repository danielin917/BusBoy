package solarcar.gui.modules;

import solarcar.vdcListener.MessageHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import java.awt.Color;


public class MPPTModule extends SolarPanel implements DataMessageSubscriber {

    //MPPT Variables
    private JLabel label;
    private DecimalFormat df;
    private String id;
    private String name;
    private MPPTTypes mppt;
    private double area;
    private double rad;
    Thread thread;
    double eff;
    double effTemp;
    double pin;
    Color labelColor;

    
    static private int drivetekcnt;
    static private int photoncnt;
    static private int mgcnt;

    public MPPTModule(MPPTTypes type, double areaIn, Color colorIn) {
        //MPPTs
        mppt = type;
        switch(type) {
            case DRIVETEK:
                id = "drivetek_" + drivetekcnt;
                name = "Drivetek " + drivetekcnt;
                drivetekcnt++;
                break;
            case PHOTON:
                id = "photon_" + photoncnt;
                name = "Photon " + photoncnt;
                photoncnt++;
                break;
            case MG:
                id = "mg_" + mgcnt;
                name = "MG " + mgcnt;
                mgcnt++;
                break;
            default:
                id = "YOU DUN GOOFED";
                break;
        }
        area = areaIn;
        labelColor = colorIn;
    }

    @Override
    public void init() {
        TitledBorder border = new TitledBorder(name);
        border.setTitleColor(labelColor);
        setBorder(border);
        
        setPreferredSize(new Dimension(140, 155));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        label = new JLabel("N/A");
        add(label);
        
        (thread=new Thread(new MPPTModule.queryThread())).start();


        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData(id, this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        //MPPTs
        switch (mppt) {
            case DRIVETEK:
                double vin = message.get("msb_uin")+message.get("lsb_uin");     //mV
                double iin = message.get("msb_iin")+message.get("lsb_iin");     //mA
                double vout = message.get("msb_uout")+message.get("lsb_uout");  //mV
                double temp = message.get("tamb");      //C
                pin = vin*iin/1000000;
                eff = pin/area/rad;
                effTemp = eff+((temp-25)*.003);
                label.setText("<html>Vin:\t" + df.format(vin/1000) + " V"
                        + "<br>Iin:\t" + df.format(iin/1000) + " A"
                        + "<br>Vout:\t" + df.format(vout/1000) + " V"
                        + "<br>Pin:\t" + df.format(pin) + " W"
                        + "<br>Temp:\t" + df.format(temp) + " °C"
                        + "<br>Area:\t" + df.format(area) + " m^2"
                        + "<br>Eff(af temp):\t" + df.format(eff*100) + "%"
                        + "<br>Eff(bf temp):\t" + df.format(effTemp*100) + "%"
                        + "</html>");
                //Add section with error flags like the MC tabs
                break;
            case PHOTON:
                break;
            case MG:
                break;
            default:
                label.setText("ERROR");
                break;
        }
    }
    
        private void query() {
        String query = "SELECT * FROM " + TelemetryGUI.db + ".wxdata ORDER BY recordedtime DESC LIMIT 1";
        Statement st = null;
        try {
            st = TelemetryGUI.getSQLConn().createStatement();
            ResultSet rs = st.executeQuery(query);
            if (rs.last()) {
//				System.out.println(rs.getDouble("solarRadiation"));
                rad = rs.getDouble("radiation");
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