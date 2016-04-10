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

public class  Speedometer extends SolarPanel implements DataMessageSubscriber {

    JLabel speedLabel;
    DecimalFormat df;

    @Override
    public void init() {
        setBorder(new TitledBorder("Speed"));
        setPreferredSize(new Dimension(120, 55));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        speedLabel = new JLabel("<html>N/A</html>");
        speedLabel.setFont(new Font(speedLabel.getFont().getFontName(),
                speedLabel.getFont().getStyle(), 30));
        speedLabel.setPreferredSize(new Dimension(300, 25));
        add(speedLabel);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData("motvel", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        //Double speed = message.getMap().get("vehvel") * 3600 / 1609.344;
        //String str = new String(speed.toString());
        //str = str.substring(0, (str.length() > 5) ? 5 : str.length());
        if (OptionsTab.get().opt_mph()) {
            speedLabel.setText("<html>" + df.format(message.get("vehvel") * 3600 / 1609.344) + "<font size=2>MPH</font></html>");
        } else if (OptionsTab.get().opt_kph()) {
            speedLabel.setText("<html>" + df.format(message.get("vehvel") * 3600 / 1000) + "<font size=2>KPH</font></html>");
        } else if (OptionsTab.get().opt_ms()) {
            speedLabel.setText("<html>" + df.format(message.get("vehvel")) + "<font size=2>M/S</font></html>");
        }
    }
}