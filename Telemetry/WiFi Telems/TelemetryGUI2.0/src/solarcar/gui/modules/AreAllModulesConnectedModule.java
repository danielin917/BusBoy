package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.tabs.OptionsTab;

public class  AreAllModulesConnectedModule extends SolarPanel implements DataMessageSubscriber {

    JLabel telemsLabel;
    int telems;

    @Override
    public void init() {
        setBorder(new TitledBorder("Modules Conn.?"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        telemsLabel = new JLabel("N/A");
        telemsLabel.setForeground(Color.BLACK);
        telemsLabel.setText("N/A");
        telemsLabel.setFont(new Font(telemsLabel.getFont().getFontName(),
                telemsLabel.getFont().getStyle(), 20));
        //		telemsLabel.setPreferredSize(new Dimension(300,50));
        add(telemsLabel);
        MessageHandler.get().subscribeData("bms_pack_volts", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        
        if(OptionsTab.get().opt_full()) {
            telemsLabel.setForeground(Color.GREEN);
            telemsLabel.setText("YES");
        }
        else {
            telemsLabel.setForeground(Color.RED);
            telemsLabel.setText("NO");
        }
        
       /* if (message.get("first_unplugged_module").intValue() == -1) {
            telemsLabel.setForeground(Color.GREEN);
            telemsLabel.setText("YES");
        } else {
            telemsLabel.setForeground(Color.RED);
            telemsLabel.setText("NO (" + message.get("first_unplugged_module").intValue() + ")");
        }*/
    }
}