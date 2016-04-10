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

public class TorqueModule extends SolarPanel implements DataMessageSubscriber {

    JLabel torqueLabel;
    DecimalFormat df;

    @Override
    public void init() {
        setBorder(new TitledBorder("Torque"));
        setPreferredSize(new Dimension(120, 55));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        torqueLabel = new JLabel("N/A");
        torqueLabel.setFont(new Font(torqueLabel.getFont().getFontName(),
                torqueLabel.getFont().getStyle(), 30));
        torqueLabel.setPreferredSize(new Dimension(300, 25));
        add(torqueLabel);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData("motcmd", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (message.get("vel") < 0) {
            torqueLabel.setForeground(Color.RED);
        } else {
            torqueLabel.setForeground(Color.BLACK);
        }
        torqueLabel.setFont(new Font(torqueLabel.getFont().getFontName(),
                torqueLabel.getFont().getStyle(), 30));
        torqueLabel.setText(df.format(message.get("trq") * 100));
    }
}
