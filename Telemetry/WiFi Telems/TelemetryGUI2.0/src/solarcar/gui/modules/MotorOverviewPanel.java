package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  MotorOverviewPanel extends SolarPanel implements DataMessageSubscriber {

    private JLabel powerName, powerVal;
    private JLabel curName, curVal;
    private double curCS;
    private DecimalFormat df;
    private double batvolttot, batvoltmain, batvoltpow;
    private int usemain;

    public MotorOverviewPanel() {
        super();
        // initialize panel
        setBorder(new TitledBorder("Motor"));
        setLayout(new GridBagLayout());

        // Initialize value labels
        curVal = new JLabel("N/A");
        powerVal = new JLabel("N/A");

        powerVal.setFont(new Font(powerVal.getFont().getFontName(), powerVal.getFont().getStyle(), 20));
        powerVal.setPreferredSize(new Dimension(100, 35));
        
        // Initialize name labels
        curName = new JLabel("Current:");
        powerName = new JLabel("Power Out:");

        powerName.setFont(new Font(powerName.getFont().getFontName(), powerName.getFont().getStyle(), 20));
        powerName.setPreferredSize(new Dimension(110,35));
        
        curName.setFont(new Font(curName.getFont().getFontName(), curName.getFont().getStyle(), 20));
        curVal.setFont(new Font(curVal.getFont().getFontName(), curVal.getFont().getStyle(), 20));
        curVal.setPreferredSize(new Dimension(100,35));

        GridBagConstraints c = new GridBagConstraints();

        // Add labels
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        add(powerName, c);
        c.gridx = 1;
        c.gridy = 0;
        add(powerVal, c);
        c.gridx = 2;
        c.gridy = 0;
        add(curName, c);
        c.gridx = 3;
        c.gridy = 0;
        add(curVal, c);

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        curCS = 0;
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("micro_current", this);
        MessageHandler.get().subscribeData("bms_pack_volts",this);
        MessageHandler.get().subscribeData("relaycon", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        switch (msg.getId()) {
            case "micro_current":
                curCS = msg.get("motor");
                break;
            case "bms_pack_volts":
                batvolttot = msg.get("pack1");
                batvoltmain = msg.get("pack0");
                break;
            case "relaycon":
                usemain = (int)(double)msg.get("shift") & 0b00000001;
                if(usemain == 0) {
                    batvoltpow = batvolttot;
                }
                else {
                    batvoltpow = batvoltmain;
                }
                break;
            default:
                break;
        }
        curVal.setText(df.format(curCS));
        powerVal.setText(df.format(curCS * batvoltpow));
    }
    
    @Override
    public String toString() {
        return "Motor overview panel";
    }

/*    protected synchronized void parseMessage(SolarDataMessage message) {
        /*
         * if(message.getId().equals("motbus")) { volt = message.get("volt");
         * if(volt < 90) MessageHandler.get().newChatMessage(new
         * SolarChatMessage("Motor controller", "Motor controller near
         * undervoltage")); }
        else
         
        switch (message.getId()) {
            case "cur0":
                cur = message.get("motor");
                break;
            case "motbackemf":
                bemfVal.setText(df.format(message.get("bemfq")));
                break;
            case "mottemp0":
                tempVal.setText(df.format(message.get("heatsink")));
                if (message.get("heatsink") > 70) {
                    tempVal.setForeground(Color.ORANGE);
                } else if (message.get("heatsink") > 80) {
                    tempVal.setForeground(Color.RED);
                } else {
                    tempVal.setForeground(Color.BLACK);
                }
                break;
        }
        imotVal.setText(df.format(cur));
        vmotVal.setText(df.format(volt));
        powerVal.setText(df.format(cur * volt));

    }*/
}
