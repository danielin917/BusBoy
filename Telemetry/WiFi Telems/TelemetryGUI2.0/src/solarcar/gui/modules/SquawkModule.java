package solarcar.gui.modules;


import java.awt.Color;
import solarcar.vdcListener.MessageHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  SquawkModule extends SolarPanel implements DataMessageSubscriber {

    JLabel squawkLabel;

    @Override
    public void init() {
        setBorder(new TitledBorder("Squawk"));
        setPreferredSize(new Dimension(120, 55));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        squawkLabel = new JLabel("N/A");
        squawkLabel.setForeground(Color.BLACK);
        squawkLabel.setText("N/A");
        squawkLabel.setFont(new Font(squawkLabel.getFont().getFontName(),
                squawkLabel.getFont().getStyle(), 20));
        //		squawkLabel.setPreferredSize(new Dimension(300,50));
        add(squawkLabel);

        MessageHandler.get().subscribeData("squawk", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.get("code").intValue()) {
            case 0:
                squawkLabel.setForeground(Color.GREEN);
                squawkLabel.setText("STDBY");
                break;

            case 1:
                squawkLabel.setForeground(Color.BLUE);
                squawkLabel.setText("ALLCLR");
                break;

            case 2:
                squawkLabel.setForeground(Color.ORANGE);
                squawkLabel.setText("NORDO");
                break;

            case 3:
                squawkLabel.setForeground(Color.ORANGE);
                squawkLabel.setText("NO VIS");
                break;

            case 4:
                squawkLabel.setForeground(Color.RED);
                squawkLabel.setText("SWAP");
                break;

            case 5:
                squawkLabel.setForeground(Color.RED);
                squawkLabel.setText("PANPAN");
                break;

            case 6:
                squawkLabel.setForeground(Color.RED);
                squawkLabel.setText("MAYDAY");
                break;
				
			case 0xfe:
                squawkLabel.setForeground(Color.BLACK);
                squawkLabel.setText("NOMSG");
                break;
				
			default:
				squawkLabel.setForeground(Color.BLACK);
				squawkLabel.setText("ERROR");
				break;
        }
    }
}