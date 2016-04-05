package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  BatteryOverviewPanel extends SolarPanel implements DataMessageSubscriber {

    private JLabel vminName, vminVal;
    private JLabel vmaxName, vmaxVal;
    private JLabel tempName, tempVal;
    private JLabel vbattotName, vbattotVal;
    private JLabel vbatmainName, vbatmainVal;
    private JLabel vbatauxName, vbatauxVal;
    private JLabel powerName, powerVal;
    private JLabel ibatBMSName, ibatBMSVal;
    private JLabel highModName, highModVal;
    private JLabel lowModName, lowModVal;
    private JLabel balanceName, balanceVal;
    private JLabel activePack;
    private DecimalFormat df;
    private DecimalFormat df2;
    private DecimalFormat df3;
    private double batvolttot, batvoltmain, batvoltaux;
    private double batvoltpow, batcur, avgTemp, maxTemp;
    private int usemain;

    public BatteryOverviewPanel() {
        super();
        // initialize panel
        setBorder(new TitledBorder("Battery"));
        //setPreferredSize(new Dimension(300,170));
        setLayout(new GridBagLayout());

        // Initialize value labels
        vminVal = new JLabel("N/A");
        vmaxVal = new JLabel("N/A");
        tempVal = new JLabel("N/A");
        vbattotVal = new JLabel("N/A");
        vbatmainVal = new JLabel("N/A");
        vbatauxVal = new JLabel("N/A");
	ibatBMSVal = new JLabel("N/A");
        powerVal = new JLabel("N/A");
        highModVal = new JLabel("N/A");
        lowModVal = new JLabel("N/A");
        balanceVal = new JLabel("N/A");
        activePack = new JLabel("PACK N/A");

        // Initialize name labels
        vminName = new JLabel("Cell Vmin:");
        vmaxName = new JLabel("Cell Vmax:");
        tempName = new JLabel("Temp:");
        vbattotName = new JLabel("Volt (Total):");
        vbatmainName = new JLabel("Volt (Main):");
        vbatauxName = new JLabel("Volt (Aux):");
	ibatBMSName = new JLabel("Current:");
        powerName = new JLabel("Power In:");
        highModName = new JLabel("High Module:");
        lowModName = new JLabel("Low Module:");
        balanceName = new JLabel("Balance:");

        powerName.setFont(new Font(powerName.getFont().getFontName(), powerName.getFont().getStyle(), 20));
        powerVal.setFont(new Font(powerVal.getFont().getFontName(), powerVal.getFont().getStyle(), 20));
        powerVal.setPreferredSize(new Dimension(100, 35));
        
        ibatBMSName.setFont(new Font(ibatBMSName.getFont().getFontName(), ibatBMSName.getFont().getStyle(), 20));
        ibatBMSVal.setFont(new Font(ibatBMSVal.getFont().getFontName(), ibatBMSVal.getFont().getStyle(), 20));
        ibatBMSVal.setPreferredSize(new Dimension(100, 35));

        activePack.setFont(new Font(activePack.getFont().getFontName(), activePack.getFont().getStyle(), 20));
        activePack.setPreferredSize(new Dimension(150, 35));
        GridBagConstraints c = new GridBagConstraints();

        // Add labels
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        add(powerName, c);
        c.gridx = 1;
        c.gridy = 0;
        add(powerVal, c);
        c.gridx = 2;
        c.gridy = 0;
        add(ibatBMSName, c);
        c.gridx = 3;
        c.gridy = 0;
        add(ibatBMSVal, c);
        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 2;
        add(activePack, c);
        
	c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        add(vbattotName, c);
        c.gridx = 1;
        c.gridy = 1;
        add(vbattotVal, c);
        c.gridx = 2;
        c.gridy = 1;
        add(vbatmainName, c);
        c.gridx = 3;
        c.gridy = 1;
        add(vbatmainVal, c);
        c.gridx = 4;
        c.gridy = 1;
        add(vbatauxName, c);
        c.gridx = 5;
        c.gridy = 1;
        add(vbatauxVal, c);
        
	c.gridx = 0;
        c.gridy = 2;
        add(vmaxName, c);
        c.gridx = 1;
        c.gridy = 2;
        add(vmaxVal, c);
        c.gridx = 2;
        c.gridy = 2;
        add(highModName, c);
        c.gridx = 3;
        c.gridy = 2;
        add(highModVal, c);
        c.gridx = 4;
        c.gridy = 2;
        add(tempName, c);
        c.gridx = 5;
        c.gridy = 2;
        add(tempVal, c);
        
        c.gridx = 0;
        c.gridy = 3;
        add(vminName, c);
        c.gridx = 1;
        c.gridy = 3;
        add(vminVal, c);
        c.gridx = 2;
        c.gridy = 3;
        add(lowModName, c);
        c.gridx = 3;
        c.gridy = 3;
        add(lowModVal, c);
        c.gridx = 4;
        c.gridy = 3;
        add(balanceName, c);
        c.gridx = 5;
        c.gridy = 3;
        add(balanceVal, c);
		
        df = new DecimalFormat("#0.000");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df2 = new DecimalFormat("#0");
		df3 = new DecimalFormat("#0.0");

        batvolttot = 0;
        batvoltmain = 0;
        batvoltaux = 0;
        batvoltpow = 0;
        batcur = 0;
    }

    @Override
    public void init() {
	MessageHandler.get().subscribeData("bms_pack_volts", this);
	MessageHandler.get().subscribeData("bmsvoltextremes", this);
	MessageHandler.get().subscribeData("ab_current", this);
        MessageHandler.get().subscribeData("bmstempextremes", this);
        MessageHandler.get().subscribeData("relaycon", this);

    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
        case "bmsvoltextremes":
            vminVal.setText(df.format(message.get("min")));
            vmaxVal.setText(df.format(message.get("max")));
            balanceVal.setText(df3.format(1000 * (message.get("balance"))) + " mV");
            highModVal.setText(df2.format(message.get("maxdex")));
            lowModVal.setText(df2.format(message.get("mindex")));
           break;
        case "bmstempextremes":
            avgTemp = (message.get("min") + message.get("max"))/2;
            maxTemp = (message.get("max"));
            tempVal.setText(df.format(maxTemp));
            if (maxTemp > 55) {
                tempVal.setForeground(Color.RED);
            } else {
                tempVal.setForeground(Color.BLACK);
            }
            break;
        case "ab_current":
            batcur = (-1)*message.get("battery");
            ibatBMSVal.setText(df.format(batcur));
            break;
        case "bms_pack_volts":
            batvolttot = message.get("pack1");
            batvoltmain = message.get("pack0");
            batvoltaux = batvolttot-batvoltmain;
            vbattotVal.setText(df.format(batvolttot));
            vbatmainVal.setText(df.format(batvoltmain));
            vbatauxVal.setText(df.format(batvoltaux));
            break;
        case "relaycon":
            usemain = (int)(double)message.get("shift") & 0b00000001;
            if(usemain == 0) {
            	batvoltpow = batvolttot;
                activePack.setText("FULL PACK");
            }
            else {
      		batvoltpow = batvoltmain;
                activePack.setText("MAIN PACK");
            }
            break;
        default:
                break;	
    }
        powerVal.setText((df.format(batvoltpow * batcur)));
    }
}
