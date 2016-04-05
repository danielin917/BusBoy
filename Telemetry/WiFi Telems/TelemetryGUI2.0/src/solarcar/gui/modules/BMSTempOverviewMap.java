package solarcar.gui.modules;

import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;

public class BMSTempOverviewMap extends SolarPanel implements DataMessageSubscriber, ItemListener {

    private DecimalFormat df;
    private JPanel moduleLabels[];
    private JLabel moduleTemperatureLabels[];
    private JPanel blankBox;
    private double[] temperatures;
    private double tmin, tmax, toffset, tscale;
    
    private static final int NUM_MODULES = 43;
    private static final int NUM_MODULE_MESSAGES = 11;

    @Override
    public void init() {
//        setBorder(new TitledBorder("BMS Overview"));
        //setPreferredSize(new Dimension(600,290));
//		setLayout(new FlowLayout(FlowLayout.LEADING));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

		setBackground(Color.white);
		
        moduleLabels = new JPanel[NUM_MODULES];
        moduleTemperatureLabels = new JLabel[NUM_MODULES];
        temperatures = new double[NUM_MODULES];

        for (int i = 0; i < NUM_MODULES; i++) {
            moduleLabels[i] = new JPanel();
            moduleLabels[i].setPreferredSize(new Dimension(80, 38));
            moduleLabels[i].setBorder(new TitledBorder("" + i));
            moduleLabels[i].setLayout(new GridBagLayout());
            moduleLabels[i].setOpaque(true);
            moduleTemperatureLabels[i] = new JLabel("N/A C");
            //moduleVoltageLabels[i].setOpaque(true);
            moduleLabels[i].add(moduleTemperatureLabels[i], c);
        }
        blankBox = new JPanel();
        blankBox.setPreferredSize(new Dimension(80,38));
        blankBox.setBorder(new TitledBorder(""));
        blankBox.setLayout(new GridBagLayout());
        blankBox.setOpaque(true);
        c.gridwidth = 1; c.gridheight = 1; c.ipadx = 0; c.ipady = 0;
        
        c.gridx = 0; c.gridy = 0; add(moduleLabels[0], c);
        c.gridx = 1; c.gridy = 0; add(moduleLabels[1], c);
        c.gridx = 2; c.gridy = 0; add(moduleLabels[2], c);
        c.gridx = 3; c.gridy = 0; add(moduleLabels[3], c);
        c.gridx = 4; c.gridy = 0; add(moduleLabels[4], c);
        c.gridx = 5; c.gridy = 0; add(moduleLabels[5], c);
        
        c.gridx = 0; c.gridy = 1; add(moduleLabels[6], c);
        c.gridx = 1; c.gridy = 1; add(moduleLabels[7], c);
        c.gridx = 2; c.gridy = 1; add(moduleLabels[8], c);
        c.gridx = 3; c.gridy = 1; add(moduleLabels[9], c);
        c.gridx = 4; c.gridy = 1; add(moduleLabels[10], c);
        c.gridx = 5; c.gridy = 1; add(moduleLabels[11], c);
        
        
        c.gridx = 0; c.gridy = 2; add(moduleLabels[12], c);
        c.gridx = 1; c.gridy = 2; add(moduleLabels[13], c);
        c.gridx = 2; c.gridy = 2; add(moduleLabels[14], c);
        c.gridx = 3; c.gridy = 2; add(moduleLabels[15], c);
        c.gridx = 4; c.gridy = 2; add(moduleLabels[16], c);
        c.gridx = 5; c.gridy = 2; add(moduleLabels[17], c);
        
        c.gridx = 0; c.gridy = 3; add(moduleLabels[18], c);
        c.gridx = 1; c.gridy = 3; add(moduleLabels[19], c);
        c.gridx = 2; c.gridy = 3; add(moduleLabels[20], c);
        c.gridx = 3; c.gridy = 3; add(moduleLabels[21], c);
        c.gridx = 4; c.gridy = 3; add(moduleLabels[22], c);
        c.gridx = 5; c.gridy = 3; add(moduleLabels[23], c);
        
        
        c.gridx = 0; c.gridy = 4; add(moduleLabels[24], c);
        c.gridx = 1; c.gridy = 4; add(moduleLabels[25], c);
        c.gridx = 2; c.gridy = 4; add(moduleLabels[26], c);
        c.gridx = 3; c.gridy = 4; add(moduleLabels[27], c);
        c.gridx = 4; c.gridy = 4; add(moduleLabels[28], c);
        c.gridx = 5; c.gridy = 4; add(moduleLabels[29], c);
        
        
        c.gridx = 0; c.gridy = 5; add(moduleLabels[30], c);
        c.gridx = 1; c.gridy = 5; add(moduleLabels[31], c);
        c.gridx = 2; c.gridy = 5; add(moduleLabels[32], c);
        c.gridx = 3; c.gridy = 5; add(moduleLabels[33], c);
        c.gridx = 4; c.gridy = 5; add(moduleLabels[34], c);   
        c.gridx = 5; c.gridy = 5; add(moduleLabels[35], c);
        
        c.gridx = 0; c.gridy = 6; add(moduleLabels[36], c);
        c.gridx = 1; c.gridy = 6; add(moduleLabels[37], c);
        c.gridx = 2; c.gridy = 6; add(moduleLabels[38], c);
        c.gridx = 3; c.gridy = 6; add(moduleLabels[39], c);
        c.gridx = 4; c.gridy = 6; add(moduleLabels[40], c);
        c.gridx = 5; c.gridy = 6; add(moduleLabels[41], c);
               
        c.gridx = 0; c.gridy = 7; add(moduleLabels[42], c);
        df = new DecimalFormat("#0.0000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        toffset = tscale = 0;

        for (int i = 0; i < NUM_MODULE_MESSAGES; i++) {
            MessageHandler.get().subscribeData("bms_temp_" + i, this);
        }
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        
        int messageNum = Integer.parseInt(message.getId().substring(9));
//        System.out.println("here");
        for (int j = 0; j < 4 && messageNum*4+j < 43; j++){
            int mod = messageNum*4 + j;
        
            if (message.get("temp_" + mod) > 45) {
                moduleTemperatureLabels[mod].setForeground(Color.RED);
            } else {
                moduleTemperatureLabels[mod].setForeground(Color.BLACK);
            }
            temperatures[mod] = message.get("temp_" + mod);
                
            moduleTemperatureLabels[mod].setText(df.format(temperatures[mod]) + " C");
        }

        tmin = Integer.MAX_VALUE;
        tmax = Integer.MIN_VALUE;
        for (int x = 0; x < NUM_MODULES; x++) {
            if (temperatures[x] < tmin) {
                tmin = temperatures[x];
            }
            if (temperatures[x] > tmax) {
                tmax = temperatures[x];
            }
        }

        toffset = (tmin + tmax) / 2;
        if (tmax - tmin > 5) {
            tscale = (tmax == tmin ? 0 : 2 / (tmax - tmin));
        } else {
            tscale = 2.0 / 5.0;
        }

        Color bk;
        for (int x = 0; x < NUM_MODULES; x++) {
            //System.out.println(voffset + " " + vscale + " " + toffset + " " + tscale);
            //System.out.println((float)Math.abs(Math.max(0,vscale*(voltages[x] - voffset))) + " " +(float)Math.min(0,vscale*(voltages[x] - voffset)) );
            float color = (float) (tscale * (temperatures[x] - toffset));

            if (color < 0) {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1 - color, 1 - color, 1);
            } else {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1, 1 - color, 1 - color);
            }

            moduleLabels[x].setBackground(bk);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        
    }

    private void showValues(JLabel labels[], boolean show) {
        /* Note: the code changes the color of the text rather than setting 
         * visible or not visible to avoid redrawing and moving labels around
         */

        for (JLabel l : labels) {
            if (!show) {
                //l.setForeground(l.getParent().getBackground());
                l.setForeground(new Color(0, 0, 0, 0));
            } else {
                l.setForeground(Color.black);
            }
        }
    }
}