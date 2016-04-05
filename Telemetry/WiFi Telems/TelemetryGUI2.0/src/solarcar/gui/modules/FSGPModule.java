package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class FSGPModule extends SolarPanel implements DataMessageSubscriber {

    JLabel powerInLabel, powerOutLabel;
    DecimalFormat df;

    @Override
    public void init() {
        setBorder(new TitledBorder("FSGP"));
        setPreferredSize(new Dimension(150, 85));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        
        powerInLabel = new JLabel("Power In: N/A");
        powerInLabel.setFont(new Font(powerInLabel.getFont().getFontName(),
        powerInLabel.getFont().getStyle(), 15));
        powerInLabel.setPreferredSize(new Dimension(300, 25));
        add(powerInLabel);
        
        powerOutLabel = new JLabel("Power Out: N/A");
        powerOutLabel.setFont(new Font(powerOutLabel.getFont().getFontName(),
        powerOutLabel.getFont().getStyle(), 15));
        powerOutLabel.setPreferredSize(new Dimension(300, 25));
        add(powerOutLabel);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData("fsgp_averages", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {      
        
        powerInLabel.setText("Power In: " +df.format(message.get("power_in")));
        powerOutLabel.setText("Power Out: " + df.format(message.get("power_out")));
    }
}
