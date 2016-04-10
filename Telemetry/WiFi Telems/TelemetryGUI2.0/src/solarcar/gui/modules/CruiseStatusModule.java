package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.gui.tabs.OptionsTab;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  CruiseStatusModule extends SolarPanel implements DataMessageSubscriber {

    JLabel set_speed, limit, set_speed_kfc, grade_kfc;
    DecimalFormat df, df2, placeHolderDF;

    @Override
    public void init() {
        setBorder(new TitledBorder("Cruise Status"));
        setPreferredSize(new Dimension(170, 100));
        setLayout(new FlowLayout(FlowLayout.LEADING));


        set_speed = new JLabel("Set Speed: N/A");
        set_speed.setPreferredSize(new Dimension(300, 15));

        limit = new JLabel("Limit: N/A");
        limit.setPreferredSize(new Dimension(300, 15));

        grade_kfc = new JLabel("Grade: N/A");
        grade_kfc.setPreferredSize(new Dimension(300, 15));
        
        add(set_speed);
        add(limit);
        add(grade_kfc);

        df = new DecimalFormat("#0.0");
        placeHolderDF = new DecimalFormat("#0.0000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        df2 = new DecimalFormat("#0.0");
        df2.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData("cruise", this);
        MessageHandler.get().subscribeData("cruisedebug", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if(message.getId().equals("cruisedebug")) {
            if (message.get("setspeed") < 0) {
                set_speed.setText("Cruise Off");
            } else if (OptionsTab.get().opt_mph()) {
                set_speed.setText("Set Speed: " + df.format(message.get("setspeed") * 3600 / 1609.344) + " MPH");
                           limit.setText("Limit: " + df.format(message.get("limit") * 3600/1609.344) + " MPH");
            } else if (OptionsTab.get().opt_kph()) {
                set_speed.setText("Set Speed: " + df.format(message.get("setspeed") * 3600 / 1000) + " KPH");
                limit.setText("Limit: " + df.format(message.get("limit") * 3600/1000 + " KPH"));
            } else if (OptionsTab.get().opt_ms()) {
                set_speed.setText("Set Speed: " + df.format(message.get("setspeed")) + " M/S");
                limit.setText("Limit: " + df.format(message.get("limit")  + " M/S"));
            }
        }
        else if (message.getId().equals("cruisedebug")) {
        grade_kfc.setText("Kalman grade: " + placeHolderDF.format(message.get("grade")) + " %");
        }
    }
}