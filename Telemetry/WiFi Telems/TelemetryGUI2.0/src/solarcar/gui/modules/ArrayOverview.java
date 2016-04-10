package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.gui.tabs.OptionsTab;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.tabs.OptionsTab;

public class  ArrayOverview extends SolarPanel implements DataMessageSubscriber {

    private JLabel powerName, powerVal;
    private JLabel curName, curVal;
    private double modulePowers[];
    private DecimalFormat df;
    private static final int NUM_MPPTS = 9;
    private double cur;
    private double volt;
    private double eff, array_size, current_radiation;
    private double batvolttot, batvoltmain;
    private int usemain;

    public ArrayOverview() {
        super();
        // initialize panel
        setBorder(new TitledBorder("Array"));
        powerName = new JLabel("Power In:");
        curName = new JLabel("Current:");

        setLayout(new GridBagLayout());

        // Initialize value labels
        powerVal = new JLabel("N/A");
        curVal = new JLabel("N/A");

        powerName.setFont(new Font(powerName.getFont().getFontName(), powerName.getFont().getStyle(), 20));
        powerVal.setFont(new Font(powerVal.getFont().getFontName(), powerVal.getFont().getStyle(), 20));
        powerVal.setPreferredSize(new Dimension(100, 35));

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

        modulePowers = new double[NUM_MPPTS];
        for (int i = 0; i < NUM_MPPTS; i++) {
            modulePowers[i] = 0;
        }

        eff = .19;      //percent eff
        array_size = 6; //m^2

        cur = 0;
        volt = 0;
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("bms_pack_volts",this);
        MessageHandler.get().subscribeData("ab_current",this);
        MessageHandler.get().subscribeData("relaycon",this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        switch (msg.getId()) {
            case "ab_current":
                cur = msg.get("array");
                break;
            case "bms_pack_volts":
                batvolttot = msg.get("pack1");
                batvoltmain = msg.get("pack0");
                break;
            case "relaycon":
                usemain = (int)(double)msg.get("shift") & 0b00000001;
                if(usemain == 0) {
                    volt = batvolttot;
                }
                else {
                    volt = batvoltmain;
                }
                break;
            default:
                break;
        }
		
        powerVal.setText(df.format(volt * cur));
        curVal.setText(df.format(cur));

        /*
         * WeatherOverviewModule method = new WeatherOverviewModule();
         * current_radiation = method.getCurrentRadiation();
         */

        if ((volt * cur) < 50) {
            powerVal.setForeground(Color.RED);
        } else if ((volt * cur) < current_radiation * array_size * eff) {
            powerVal.setForeground(Color.ORANGE);
        } else {
            powerVal.setForeground(Color.BLACK);
        }
    }

    protected synchronized void parseMessage(SolarDataMessage message) {
    	if (message.getId().equals("bms_pack_volts")) {
        	if(OptionsTab.get().opt_full()) {
        		volt = message.get("pack1");
        	}
        	else {
        		volt = message.get("pack0");
        	}
        }

        powerVal.setText(df.format(volt * cur));

        if ((volt * cur) < 50) {
            powerVal.setForeground(Color.RED);
        } else if ((volt * cur) < current_radiation * array_size * eff) {
            powerVal.setForeground(Color.ORANGE);
        } else {
            powerVal.setForeground(Color.BLACK);
        }
    }
}
