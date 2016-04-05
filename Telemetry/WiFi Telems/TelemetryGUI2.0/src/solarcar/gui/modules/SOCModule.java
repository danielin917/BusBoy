package solarcar.gui.modules;

import solarcar.vdcListener.MessageHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.gui.tabs.OptionsTab;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  SOCModule extends SolarPanel implements DataMessageSubscriber {

    JLabel SOCLabel;
    DecimalFormat df;

    public SOCModule() {
        super();
        setBorder(new TitledBorder("SOC"));
        setPreferredSize(new Dimension(120, 55));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        SOCLabel = new JLabel("N/A");
        SOCLabel.setFont(new Font(SOCLabel.getFont().getFontName(),
                SOCLabel.getFont().getStyle(), 30));
        SOCLabel.setPreferredSize(new Dimension(300, 25));
        add(SOCLabel);

        df = new DecimalFormat("##0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("SOC", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
       // SOCLabel.setText(df.format(msg.get("SOC_master") + OptionsTab.get().SOCOffset()) + "%");
            SOCLabel.setText(df.format(msg.get("SOC") + OptionsTab.get().SOCOffset()) + "%");

            }
}
