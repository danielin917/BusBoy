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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;

public class BMSOverviewMap extends SolarPanel implements DataMessageSubscriber, ItemListener {

    private DecimalFormat df;
    private JPanel moduleLabels[];
    private JLabel moduleVoltageLabels[];
    private JLabel moduleTempLabels[];
    private JLabel moduleBalanceLabels[];
    private JPanel valuesDisplayed;
    private JPanel valuesColorized;
    private JPanel colorScalePanel;
	private JLabel topLabel;
	private JLabel bottomLabel;
    private double[] voltages;
    private double[] temperatures;
    private double vmin, vmax, tmin, tmax, voffset, vscale, toffset, tscale;
    private JRadioButton voltColorized;
    private JRadioButton tempColorized;
    private JCheckBox voltDisplayed;
    private JCheckBox tempDisplayed;
    private ButtonGroup select;
    boolean showVolt;
    private Color chargeBalanceForegroundColor = Color.black;
    private Color chargeBalanceBackgroundColor = Color.yellow;
    private Color dischargeBalanceForegroundColor = Color.white;
    private Color dischargeBalanceBackgroundColor = Color.red;
    private static final int NUM_MODULES = 42;
    private static final int NUM_MODULE_MESSAGES = 10;

    @Override
    public void init() {
//        setBorder(new TitledBorder("BMS Overview"));
        //setPreferredSize(new Dimension(600,290));
//		setLayout(new FlowLayout(FlowLayout.LEADING));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

		topLabel = new JLabel("Top Box");
		bottomLabel = new JLabel("Bottom Box");
		
        moduleLabels = new JPanel[NUM_MODULES];
        moduleVoltageLabels = new JLabel[NUM_MODULES];
        moduleTempLabels = new JLabel[NUM_MODULES];
        moduleBalanceLabels = new JLabel[NUM_MODULES];
        voltages = new double[NUM_MODULES];
        temperatures = new double[NUM_MODULES];

        for (int i = 0; i < NUM_MODULES; i++) {
            moduleLabels[i] = new JPanel();
            moduleLabels[i].setPreferredSize(new Dimension(80, 70));
            moduleLabels[i].setBorder(new TitledBorder("" + i));
            moduleLabels[i].setOpaque(true);
            moduleLabels[i].setLayout(new GridBagLayout());

            moduleVoltageLabels[i] = new JLabel("N/A V");
            moduleTempLabels[i] = new JLabel("N/A C");
            moduleBalanceLabels[i] = new JLabel(" ");
            moduleVoltageLabels[i].setOpaque(true);
            moduleTempLabels[i].setOpaque(true);
            moduleBalanceLabels[i].setOpaque(true);

            c.gridheight = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.ipadx = 5;
            c.ipady = 5;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;

            c.gridx = 0;
            c.gridy = 0;
            moduleLabels[i].add(moduleVoltageLabels[i], c);
            c.gridx = 0;
            c.gridy = 1;
            moduleLabels[i].add(moduleTempLabels[i], c);
            c.gridx = 0;
            c.gridy = 2;
            moduleLabels[i].add(moduleBalanceLabels[i], c);

            voltages[i] = 0;
            temperatures[i] = 0;
        }

        valuesDisplayed = new JPanel();
        valuesDisplayed.setBorder(new TitledBorder("Values displayed"));
        valuesDisplayed.setOpaque(true);
        valuesDisplayed.setLayout(new GridBagLayout());

        voltDisplayed = new JCheckBox("volt", true);
        tempDisplayed = new JCheckBox("temp", true);

        voltDisplayed.addItemListener(this);
        tempDisplayed.addItemListener(this);

        c.gridx = 0; c.gridy = 0; valuesDisplayed.add(voltDisplayed, c);
        c.gridx = 0; c.gridy = 1; valuesDisplayed.add(tempDisplayed, c);

        valuesColorized = new JPanel();
        valuesColorized.setBorder(new TitledBorder("Values colorized"));
        valuesColorized.setOpaque(true);
        valuesColorized.setLayout(new GridBagLayout());

        voltColorized = new JRadioButton("volt");
        tempColorized = new JRadioButton("temp");
        select = new ButtonGroup();
        select.add(voltColorized);
        select.add(tempColorized);
        voltColorized.setSelected(true);
        
        c.gridx = 0; c.gridy = 0; valuesColorized.add(voltColorized, c);
        c.gridx = 0; c.gridy = 1; valuesColorized.add(tempColorized, c);

        c.gridwidth = 1; c.gridheight = 1; c.ipadx = 0; c.ipady = 0;

        c.gridx = 0; c.gridy = 4; add(moduleLabels[0], c);
        c.gridx = 1; c.gridy = 4; add(moduleLabels[1], c);
        c.gridx = 1; c.gridy = 3; add(moduleLabels[2], c);
        c.gridx = 0; c.gridy = 3; add(moduleLabels[3], c);
        c.gridx = 0; c.gridy = 2; add(moduleLabels[4], c);
        c.gridx = 1; c.gridy = 2; add(moduleLabels[5], c);
        c.gridx = 1; c.gridy = 1; add(moduleLabels[6], c);
        c.gridx = 0; c.gridy = 1; add(moduleLabels[7], c);
        c.gridx = 0; c.gridy = 0; add(moduleLabels[8], c);
        c.gridx = 1; c.gridy = 0; add(moduleLabels[9], c);
        c.gridx = 2; c.gridy = 0; add(moduleLabels[10], c);
        c.gridx = 2; c.gridy = 1; add(moduleLabels[13], c);
        c.gridx = 2; c.gridy = 2; add(moduleLabels[14], c);
        c.gridx = 2; c.gridy = 3; add(moduleLabels[17], c);
        c.gridx = 2; c.gridy = 4; add(moduleLabels[18], c);
        
        c.insets.right = 15;
        
        c.gridx = 3; c.gridy = 0; add(moduleLabels[11], c);
        c.gridx = 3; c.gridy = 1; add(moduleLabels[12], c);
        c.gridx = 3; c.gridy = 2; add(moduleLabels[15], c);
        c.gridx = 3; c.gridy = 3; add(moduleLabels[16], c);
        c.gridx = 3; c.gridy = 4; add(moduleLabels[19], c);

        c.insets.right = 0;
        c.gridx = 6; c.gridy = 3; add(moduleLabels[20], c);
        c.gridx = 5; c.gridy = 3; add(moduleLabels[21], c);
        c.gridx = 5; c.gridy = 2; add(moduleLabels[22], c);
        c.gridx = 6; c.gridy = 2; add(moduleLabels[23], c);
        c.gridx = 6; c.gridy = 1; add(moduleLabels[24], c);
        c.gridx = 5; c.gridy = 1; add(moduleLabels[25], c);
        c.gridx = 5; c.gridy = 0; add(moduleLabels[26], c);
        c.gridx = 6; c.gridy = 0; add(moduleLabels[27], c);
        c.gridx = 7; c.gridy = 0; add(moduleLabels[28], c);
        c.gridx = 8; c.gridy = 0;        add(moduleLabels[29], c);
        c.gridx = 8; c.gridy = 1;        add(moduleLabels[30], c);
        c.gridx = 7; c.gridy = 1;        add(moduleLabels[31], c);
        c.gridx = 7; c.gridy = 2;        add(moduleLabels[32], c);       
        c.gridx = 8; c.gridy = 2;    add(moduleLabels[33], c);
        c.gridx = 8; c.gridy = 3;        add(moduleLabels[34], c);
        c.gridx = 7; c.gridy = 3;        add(moduleLabels[35], c);
        c.gridx = 5; c.gridy = 4;        add(moduleLabels[36], c);
        c.gridx = 6; c.gridy = 4;        add(moduleLabels[37], c);
        c.gridx = 7; c.gridy = 4;        add(moduleLabels[38], c);
        c.gridx = 8; c.gridy = 4;        add(moduleLabels[39], c);
        c.gridx = 9;  c.gridy = 4;        add(moduleLabels[40], c);
        c.gridx = 9;        c.gridy = 3;        add(moduleLabels[41], c);
        c.gridx = 9;        c.gridy = 2;        add(moduleLabels[42], c);

        c.gridwidth = 2;
        c.gridheight = 2;
        c.gridx = 5;
        c.gridy = 4;
        add(valuesDisplayed, c);
        c.gridx = 7;
        c.gridy = 4;
        add(valuesColorized, c);

        colorScalePanel = new JPanel();
        colorScalePanel.setBorder(new TitledBorder("Color scale (currently not implemented)"));

        c.gridwidth = 1;
        c.gridheight = 5;
        c.gridx = 10;
        c.gridy = 0;
        add(colorScalePanel, c);

        df = new DecimalFormat("#0.0000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        toffset = tscale = voffset = vscale = 0;

        for (int i = 0; i < NUM_MODULE_MESSAGES; i++) {
            MessageHandler.get().subscribeData("bms_volts_" + i, this);
        }
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        int messageNum = Integer.parseInt(message.getId().substring(10));
        int mod1 = messageNum;
        int mod2 = messageNum+1;
        int mod3 = messageNum+2;
        int mod4 = messageNum+3;
        if (message.get("volt_" + mod1) > 4.2 || message.get("volt_" + mod1) < 2./* || message.get("therm") > 45*/) {
            moduleVoltageLabels[mod1].setForeground(Color.RED);
            moduleTempLabels[mod1].setForeground(Color.RED);
        }
 
        voltages[mod1] = message.get("volt_" + mod1);
        //temperatures[mod1] = message.get("therm");

        moduleVoltageLabels[mod1].setText(df.format(voltages[mod1]) + " V");
        //moduleTempLabels[mod1].setText(df.format(temperatures[mod1]) + " C");


        vmin = Integer.MAX_VALUE;
        vmax = Integer.MIN_VALUE;
        tmax = Integer.MIN_VALUE;
        tmin = Integer.MAX_VALUE;
        for (int x = 0; x < NUM_MODULES; x++) {
            if (voltages[x] < vmin) {
                vmin = voltages[x];
            }
            if (voltages[x] > vmax) {
                vmax = voltages[x];
            }
            if (temperatures[x] > tmax) {
                tmax = temperatures[x];
            }
            if (temperatures[x] < tmin) {
                tmin = temperatures[x];
            }
        }

        voffset = (vmin + vmax) / 2;
        if (vmax - vmin > 0.05) {
            vscale = (vmax == vmin ? 0 : 2 / (vmax - vmin));
        } else {
            vscale = 2 / 0.05;
        }

        toffset = (tmin + tmax) / 2;
        if (tmax - tmin > 4) {
            tscale = (tmax == tmin ? 0 : 2 / (tmax - tmin));
        } else {
            tscale = 2.0 / 4;
        }

//        if (voltColorized.isSelected()) {

        Color bk;
        for (int x = 0; x < NUM_MODULES; x++) {
            //System.out.println(voffset + " " + vscale + " " + toffset + " " + tscale);
            //System.out.println((float)Math.abs(Math.max(0,vscale*(voltages[x] - voffset))) + " " +(float)Math.min(0,vscale*(voltages[x] - voffset)) );
            float color = (float) (vscale * (voltages[x] - voffset));

            if (color < 0) {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1 - color, 1 - color, 1);
            } else {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1, 1 - color, 1 - color);
            }

            moduleVoltageLabels[x].setBackground(bk);
        }
//        } else if (tempColorized.isSelected()) {

//            Color bk;
        for (int x = 0; x < NUM_MODULES; x++) {
            float color = (float) (tscale * (temperatures[x] - toffset));

            if (color < 0) {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1 - color, 1 - color, 1);
            } else {
                color = Math.abs(color) * 0.75f;
                bk = new Color(1, 1 - color, 1 - color);
            }

            moduleTempLabels[x].setBackground(bk);
        }
//        } else {
//        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == voltDisplayed) {
            showValues(moduleVoltageLabels, voltDisplayed.isSelected());
        } else if (source == tempDisplayed) {
            showValues(moduleTempLabels, tempDisplayed.isSelected());
        }
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