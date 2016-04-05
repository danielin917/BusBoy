package solarcar.gui.modules;


import java.awt.Color;
import solarcar.vdcListener.MessageHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import java.math.RoundingMode;


public class  RegenModule extends SolarPanel implements DataMessageSubscriber {

    //uses similar method as steering regen limiting. Doens't depend on current power out
    
    JLabel regenLabel;
    private double vmax = 0;
    private double speed = 0;
    private double power_able = 0; //power able to push back into the battery
    private double torque_able = 0;
    private static double MAX_CELL_VOLTAGE = 4.15; //to be safe
    private static double MOTOR_EFF = 98.6;
    private static double NUMBER_OF_MODULES = 40;
    private static double NUMBER_OF_CELLS = 10;
    private static double MODULE_RESISTANCE = .3/NUMBER_OF_CELLS; //this may need to change (.1 for 2014
    private static double RADIUS = .27635;
    private DecimalFormat df;

            

    @Override
    public void init() {
        setBorder(new TitledBorder("Regen Limit"));
        setPreferredSize(new Dimension(120, 55));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        regenLabel = new JLabel("N/A");
        regenLabel.setForeground(Color.BLACK);
        regenLabel.setText("N/A");
        regenLabel.setFont(new Font(regenLabel.getFont().getFontName(),
        regenLabel.getFont().getStyle(), 20));
        add(regenLabel);

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        
        MessageHandler.get().subscribeData("bmsvoltextremes",this);
        MessageHandler.get().subscribeData("motvel",this);

        
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "bmsvoltextremes":
                //something main, aux
                vmax = message.get("max");
                power_able = ((MAX_CELL_VOLTAGE - vmax)/MODULE_RESISTANCE)*vmax*NUMBER_OF_MODULES;
                break;

            case "motvel":
                speed = message.get("vehvel");
                torque_able = power_able/speed*RADIUS;
                regenLabel.setText(df.format(torque_able*.88317704));
                break;
				
            default:
		break;
        }
    }
}