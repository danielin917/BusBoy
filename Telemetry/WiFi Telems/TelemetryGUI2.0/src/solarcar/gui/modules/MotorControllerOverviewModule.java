package solarcar.gui.modules;

import solarcar.vdcListener.MessageHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class MotorControllerOverviewModule extends SolarPanel implements DataMessageSubscriber {

    JLabel lim, err, txErr, rxErr;
    JLabel lim2, err2, txErr2, rxErr2;

    DecimalFormat df;

    @Override
    public void init() {
        setBorder(new TitledBorder("Motor Ctrl Status"));
        setPreferredSize(new Dimension(200, 110));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // setLayout(new FlowLayout(FlowLayout.LEADING));
        lim = new JLabel("Limit 1: N/A");
        lim.setPreferredSize(new Dimension(100, 15));

        err = new JLabel("Error 1: N/A");
        err.setPreferredSize(new Dimension(100, 15));

        txErr = new JLabel("TXErr 1: N/A");
        txErr.setPreferredSize(new Dimension(80, 15));

        rxErr = new JLabel("RXErr 1: N/A");
        rxErr.setPreferredSize(new Dimension(80, 15));

        lim2 = new JLabel("Limit 2: N/A");
        lim2.setPreferredSize(new Dimension(100, 15));

        err2 = new JLabel("Error 2: N/A");
        err2.setPreferredSize(new Dimension(100, 15));

        txErr2 = new JLabel("TXErr 2: N/A");
        txErr2.setPreferredSize(new Dimension(80, 15));

        rxErr2 = new JLabel("RXErr 2: N/A");
        rxErr2.setPreferredSize(new Dimension(80, 15));

        c.ipadx = 5;
        c.ipady = 5;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        add(lim, c);
        c.gridx = 0;
        c.gridy = 1;
        add(err, c);
        c.gridx = 0;
        c.gridy = 2;
        add(lim2, c);
        c.gridx = 0;
        c.gridy = 3;
        add(err2, c);
        
        c.gridx = 1;
        c.gridy = 0;
        add(txErr, c);
        c.gridx = 1;
        c.gridy = 1;
        add(rxErr, c);
        c.gridx = 1;
        c.gridy = 2;
        add(txErr2, c);
        c.gridx = 1;
        c.gridy = 3;
        add(rxErr2, c);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        MessageHandler.get().subscribeData("motflag", this);
        MessageHandler.get().subscribeData("motflag2", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "motflag":
                lim.setText("Limit 1: (" + message.get("lim") + ") ");
                
                int error = (int) (double) message.get("err");
                if (error == 0) {
                    err.setText("Error 1: OK (" + message.get("err") + ")");
                    err.setForeground(Color.BLACK);
                } else {
                    err.setText("Error 1: (" + message.get("err") + ") ");
                    err.setForeground(Color.RED);
                }
                
                txErr.setText("TXErr 1: " + (int) (double) message.get("txerr"));
                rxErr.setText("RXErr 1: " + (int) (double) message.get("rxerr"));
                break;
            case "motflag2":
                lim2.setText("Limit 2: (" + message.get("lim") + ") ");

                int error2 = (int) (double) message.get("err");
                if (error2 == 0) {
                    err2.setText("Error 2: OK (" + message.get("err") + ")");
                    err2.setForeground(Color.BLACK);
                } else {
                    err2.setText("Error 2: (" + message.get("err") + ") ");
                    err2.setForeground(Color.RED);
                }
                
                txErr2.setText("TXErr 2: " + (int) (double) message.get("txerr"));
                rxErr2.setText("RXErr 2: " + (int) (double) message.get("rxerr"));
                break;
            
            default:
                break;
        }
    }
}
