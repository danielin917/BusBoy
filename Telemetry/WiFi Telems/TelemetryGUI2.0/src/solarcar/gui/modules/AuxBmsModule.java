package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  AuxBmsModule extends SolarPanel implements DataMessageSubscriber {

    JLabel voltage1, voltage0, therm1, therm0, error1, error0, title1, title0;
    DecimalFormat df;

    @Override
    public void init() {

        setBorder(new TitledBorder("Aux Pack"));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(250, 170));

        voltage1 = new JLabel("Volt: N/A");
        therm1 = new JLabel("Temp: N/A");
        error1 = new JLabel("Error: N/A");
        title1 = new JLabel("Aux 1");
        
        voltage0 = new JLabel("Volt: N/A");
        therm0 = new JLabel("Temp: N/A");
        error0 = new JLabel("Error: N/A");
        title0 = new JLabel("Aux 0");
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1; c.gridheight = 1; c.ipadx = 14; c.ipady = 10;

        df = new DecimalFormat("0.00");
        
        c.gridx = 0; c.gridy = 0; add(title0, c);
        c.gridx = 1; c.gridy = 0; add(title1, c);
        
        c.gridx = 0; c.gridy = 1; add(voltage0, c);
        c.gridx = 1; c.gridy = 1; add(voltage1, c);
        
        c.gridx = 0; c.gridy = 2; add(therm0,c);
        c.gridx = 1; c.gridy = 2; add(therm1, c);
        
        c.gridx = 0; c.gridy = 3; add(error0,c);
        c.gridx = 1; c.gridy = 3; add(error1,c);

        MessageHandler.get().subscribeData("auxbms0", this);
        MessageHandler.get().subscribeData("auxbms1",this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (message.getId().equals("auxbms0")) {
            voltage0.setText("Volt: " + df.format(message.get("voltage")));
            therm0.setText("Temp: " + df.format(message.get("therm")));
            error0.setText("Error: " + df.format(message.get("error")));
        }
        else if (message.getId().equals("auxbms1")) {
            voltage1.setText("Volt: " + df.format(message.get("voltage")));
            therm1.setText("Temp: " + df.format(message.get("therm")));
            error1.setText("Error: " + df.format(message.get("error")));
        }
    }
}