package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.gui.tabs.OptionsTab;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.VDCConn;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

public class  CruiseCommandModule extends SolarPanel implements DataMessageSubscriber, ActionListener {

    JLabel set_speed, limit;
    JSpinner cruiseSetSpinner;
    JSpinner cruiseLimitSpinner;
    SpinnerNumberModel cruiseSetModel;
    SpinnerNumberModel cruiseLimitModel;
    JButton sendSpeedToBlueButton;
    JButton sendLimitToBlueButton;
    DecimalFormat df, df2;

    @Override
    public void init() {
        setBorder(new TitledBorder("Cruise Commands"));
        setPreferredSize(new Dimension(150, 180));
        setLayout(new FlowLayout(FlowLayout.LEADING));


        set_speed = new JLabel("Set Speed: N/A");
        set_speed.setPreferredSize(new Dimension(300, 15));

        limit = new JLabel("Limit: N/A");
        limit.setPreferredSize(new Dimension(300, 15));

        cruiseSetModel = new SpinnerNumberModel(0.0f, 0.0f, 88.0f, 0.5f);
        cruiseSetSpinner = new JSpinner(cruiseSetModel);
        
        cruiseLimitModel = new SpinnerNumberModel(0.0f, 0.0f, 9000.0f, 0.5f);
        cruiseLimitSpinner = new JSpinner(cruiseLimitModel);

        sendSpeedToBlueButton = new JButton("Send Speed");
        sendSpeedToBlueButton.addActionListener(this);
        
        sendLimitToBlueButton = new JButton("Send Limit");
        sendLimitToBlueButton.addActionListener(this);

        add(set_speed);
        add(limit);
        add(cruiseSetSpinner);
        add(sendSpeedToBlueButton);
        add(cruiseLimitSpinner);
        add(sendLimitToBlueButton);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        df2 = new DecimalFormat("#0.0");
        df2.setRoundingMode(RoundingMode.HALF_UP);

        MessageHandler.get().subscribeData("cruise", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (OptionsTab.get().opt_mph()) {
            set_speed.setText("Set Speed: " + df.format(message.get("speed") * 3600 / 1609.344) + " MPH");
        } else if (OptionsTab.get().opt_kph()) {
            set_speed.setText("Set Speed: " + df.format(message.get("speed") * 3600 / 1000) + " KPH");
        } else if (OptionsTab.get().opt_ms()) {
            set_speed.setText("Set Speed: " + df.format(message.get("speed")) + " M/S");
        }
        limit.setText("Limit: " + df.format(message.get("limit") * 100));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        float cc_set = -1;
        if (e.getActionCommand().equals("Send Speed")) {
            if (OptionsTab.get().opt_mph()) {
                cc_set = (float) ((Double) cruiseSetModel.getNumber() * 1609.344 / 3600);
            } else if (OptionsTab.get().opt_kph()) {
                cc_set = (float) ((Double) cruiseSetModel.getNumber() * 1000 / 3600);
            } else if (OptionsTab.get().opt_ms()) {
                cc_set = ((float) cruiseSetModel.getNumber()); //6/21/2014 this used to be a (Float) which I think is wrong.
            }
            
            String speed = Integer.toHexString(Integer.reverseBytes(Float.floatToIntBits(cc_set)));
            System.out.println("Set speed: " + cc_set);
            VDCConn.get().sendMessage("can addr='0x225' data='0x" + speed + "' len='4' priority='0' ext='0' rem='0'");
            //Copy code from chat here
        }
        
        if (e.getActionCommand().equals("Send Limit")) {
            if (OptionsTab.get().opt_mph()) {
                cc_set = (float) ((Double) cruiseLimitModel.getNumber() * 1609.344 / 3600);
            } else if (OptionsTab.get().opt_kph()) {
                cc_set = (float) ((Double) cruiseLimitModel.getNumber() * 1000 / 3600);
            } else if (OptionsTab.get().opt_ms()) {
                cc_set = ((float) cruiseLimitModel.getNumber());
            }
            
            String speed = Integer.toHexString(Integer.reverseBytes(Float.floatToIntBits(cc_set)));
            System.out.println("Limit speed: " + cc_set);
            VDCConn.get().sendMessage("can addr='0x22a' data='0x" + speed + "' len='4' priority='0' ext='0' rem='0'");
            //Copy code from chat here
        }
    }
}
