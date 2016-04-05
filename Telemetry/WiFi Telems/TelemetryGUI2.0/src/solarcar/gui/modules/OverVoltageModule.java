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

public class  OverVoltageModule extends SolarPanel implements DataMessageSubscriber {

    JLabel telemsLabel;
    int telems;
    private JLabel vmaxVal;
    private DecimalFormat df;

    @Override
    public void init() {

        setBorder(new TitledBorder("Voltage Status"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        telemsLabel = new JLabel("N/A");
        telemsLabel.setForeground(Color.BLACK);
        telemsLabel.setText("N/A");
        telemsLabel.setFont(new Font(telemsLabel.getFont().getFontName(),
                telemsLabel.getFont().getStyle(), 20));
        //		telemsLabel.setPreferredSize(new Dimension(300,50));
        add(telemsLabel);

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        vmaxVal = new JLabel("N/A");

        MessageHandler.get().subscribeData("bmsvoltsextremes", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        //vminVal.setText(df.format(message.get("vmin")));
        vmaxVal.setText(df.format(message.get("max")));
        if (Double.parseDouble(df.format(message.get("max"))) < 4.15 && Double.parseDouble(df.format(message.get("vmin"))) > 3) {
            telemsLabel.setForeground(Color.GREEN);
            telemsLabel.setText("NORMAL");
        } else if (Double.parseDouble(df.format(message.get("min"))) < 3) {
            telemsLabel.setForeground(Color.ORANGE);
            telemsLabel.setText("LOW");
        }  else if (Double.parseDouble(df.format(message.get("max"))) > 4.1) {
            telemsLabel.setForeground(Color.ORANGE);
            telemsLabel.setText("HIGH");
        } else if (Double.parseDouble(df.format(message.get("min"))) < 2.5) {
            telemsLabel.setForeground(Color.RED);
            telemsLabel.setText("UNDER");
        } else if (Double.parseDouble(df.format(message.get("max"))) > 4.2) {
            telemsLabel.setForeground(Color.RED);
            telemsLabel.setText("OVER");
        }
        //tempVal.setText(df.format(message.get("temp")));
    }
}