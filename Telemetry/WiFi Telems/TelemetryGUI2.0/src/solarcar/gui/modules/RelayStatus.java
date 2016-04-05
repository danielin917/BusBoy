package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  RelayStatus extends SolarPanel implements DataMessageSubscriber {

    JLabel relayLabel;

    @Override
    public void init() {
        setBorder(new TitledBorder("Relay"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        relayLabel = new JLabel("N/A");
        relayLabel.setForeground(Color.BLACK);
        relayLabel.setText("N/A");
        relayLabel.setFont(new Font(relayLabel.getFont().getFontName(),
                relayLabel.getFont().getStyle(), 20));
        //		relayLabel.setPreferredSize(new Dimension(300,50));
        add(relayLabel);

        MessageHandler.get().subscribeData("RELAY_CTRL_STATUS", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (message.get("RELAY_CTRL_STATUS").intValue() == 1) {
            relayLabel.setForeground(Color.GREEN);
            relayLabel.setText("CLOSED");
        } else {
            relayLabel.setForeground(Color.RED);
            relayLabel.setText("OPEN");
        }
    }
}