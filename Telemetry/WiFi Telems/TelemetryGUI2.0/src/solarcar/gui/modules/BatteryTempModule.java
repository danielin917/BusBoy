package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  BatteryTempModule extends SolarPanel implements DataMessageSubscriber {

    JLabel telemsLabel;
    int telems;
    private JLabel tempVal;
    private DecimalFormat df;

    @Override
    public void init() {

        setBorder(new TitledBorder("Battery Temp"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        telemsLabel = new JLabel("N/A");
        telemsLabel.setForeground(Color.BLACK);
        telemsLabel.setText("N/A");
        telemsLabel.setFont(new Font(telemsLabel.getFont().getFontName(),
                telemsLabel.getFont().getStyle(), 20));
        //		telemsLabel.setPreferredSize(new Dimension(300,50));
        add(telemsLabel);

        df = new DecimalFormat("#0.0");
        tempVal = new JLabel("N/A");

        MessageHandler.get().subscribeData("bmstempextremes", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        double  avgTemp = (message.get("min") + message.get("max"))/2;

        if (avgTemp > 42) {
            telemsLabel.setForeground(Color.RED);
            telemsLabel.setText("HOT");
        } else if (avgTemp > 38) {
            telemsLabel.setForeground(Color.ORANGE);
            telemsLabel.setText("WARM");
        } else {
            telemsLabel.setForeground(Color.GREEN);
            telemsLabel.setText("NORMAL");
        }
    }
}