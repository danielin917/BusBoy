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

public class  BMSAutoCalMap extends SolarPanel implements DataMessageSubscriber, ItemListener {

    private DecimalFormat df;
	private DecimalFormat dfInt;
    private JPanel moduleLabels[];
	private JPanel valuesDisplayed;
	private JPanel colorizedValue;
	private JLabel moduleGains[], moduleOffsets[];
	private JLabel moduleRef[], moduleZero[], moduleRefVolt[], moduleZeroVolt[];
    private double[] gains;
    private double[] offsets;
    private double gainMin, gainMax, offsetMin, offsetMax, gainOffset, gainScale, offsetOffset, offsetScale;
    private JCheckBox moduleGainEnabled, moduleOffsetEnabled;
	private JCheckBox moduleRefEnabled, moduleZeroEnabled;
	private JCheckBox moduleRefVoltEnabled, moduleZeroVoltEnabled;
	private JRadioButton moduleGainColorized, moduleOffsetColorized;
    private ButtonGroup select;
    boolean showVolt;
    private static final int NUM_MODULES = 6;

    @Override
    public void init() {
//        setBorder(new TitledBorder("BMS Autocalibration"));
        //setPreferredSize(new Dimension(600,290));
//		setLayout(new FlowLayout(FlowLayout.LEADING));
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        moduleLabels = new JPanel[NUM_MODULES];
		moduleGains = new JLabel[NUM_MODULES];
		moduleOffsets = new JLabel[NUM_MODULES];
		moduleRef = new JLabel[NUM_MODULES];
		moduleZero = new JLabel[NUM_MODULES];
		moduleRefVolt = new JLabel[NUM_MODULES];
		moduleZeroVolt = new JLabel[NUM_MODULES];
        gains = new double[NUM_MODULES];
        offsets = new double[NUM_MODULES];

        for (int i = 0; i < NUM_MODULES; i++) {
            moduleLabels[i] = new JPanel();
            moduleLabels[i].setPreferredSize(new Dimension(200, 70));
            moduleLabels[i].setBorder(new TitledBorder("" + i));
            moduleLabels[i].setOpaque(true);
			
			moduleGains[i] = new JLabel("Gain: N/A");
			moduleOffsets[i] = new JLabel("Offset: N/A");
			moduleRef[i] = new JLabel("Ref: N/A");
			moduleZero[i] = new JLabel("Zero: N/A");
			moduleRefVolt[i] = new JLabel("Volt: N/A");
			moduleZeroVolt[i] = new JLabel("Volt: N/A");
			
			moduleLabels[i].setLayout(new GridBagLayout());
			
			c.gridheight = 1; c.gridwidth = 1; 
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.ipadx = 5; c.ipady = 1;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1; c.weighty = 1;
			
			c.gridx = 0; c.gridy = 0; moduleLabels[i].add(moduleGains[i], c);
			c.gridx = 1; c.gridy = 0; moduleLabels[i].add(moduleOffsets[i], c);
			
			c.gridx = 0; c.gridy = 1; moduleLabels[i].add(moduleRef[i], c);
			c.gridx = 1; c.gridy = 1; moduleLabels[i].add(moduleZero[i], c);
			
			c.gridx = 0; c.gridy = 2; moduleLabels[i].add(moduleRefVolt[i], c);
			c.gridx = 1; c.gridy = 2; moduleLabels[i].add(moduleZeroVolt[i], c);
			
            gains[i] = 0;
            offsets[i] = 0;
        }
		
		valuesDisplayed = new JPanel();
		valuesDisplayed.setBorder(new TitledBorder("Values displayed"));
		valuesDisplayed.setOpaque(true);
		valuesDisplayed.setLayout(new GridBagLayout());
		
		moduleGainEnabled = new JCheckBox("Gain", true);
		moduleOffsetEnabled = new JCheckBox("Offset", true);
		moduleRefEnabled = new JCheckBox("Ref", true);
		moduleZeroEnabled = new JCheckBox("Zero", true);
		moduleRefVoltEnabled = new JCheckBox("Ref Volt", true);
		moduleZeroVoltEnabled = new JCheckBox("Zero Volt", true);
		
		moduleGainEnabled.addItemListener(this);
		moduleOffsetEnabled.addItemListener(this);
		moduleRefEnabled.addItemListener(this);
		moduleZeroEnabled.addItemListener(this);
		moduleRefVoltEnabled.addItemListener(this);
		moduleZeroVoltEnabled.addItemListener(this);
		
		c.gridheight = 1; c.gridwidth = 1;
		c.ipadx = 5; c.ipady = 0;

		c.gridx = 0; c.gridy = 0; valuesDisplayed.add(moduleGainEnabled, c);
		c.gridx = 1; c.gridy = 0; valuesDisplayed.add(moduleOffsetEnabled, c);

		c.gridx = 0; c.gridy = 1; valuesDisplayed.add(moduleRefEnabled, c);
		c.gridx = 1; c.gridy = 1; valuesDisplayed.add(moduleZeroEnabled, c);

		c.gridx = 0; c.gridy = 2; valuesDisplayed.add(moduleRefVoltEnabled, c);
		c.gridx = 1; c.gridy = 2; valuesDisplayed.add(moduleZeroVoltEnabled, c);

		colorizedValue = new JPanel();
		colorizedValue.setBorder(new TitledBorder("Colorized value"));
		colorizedValue.setOpaque(true);
		colorizedValue.setLayout(new GridBagLayout());
		
		moduleGainColorized = new JRadioButton("Gain", true);
		moduleOffsetColorized = new JRadioButton("Offset", false);
		
		c.gridx = 0; c.gridy = 0; colorizedValue.add(moduleGainColorized, c);
		c.gridx = 1; c.gridy = 0; colorizedValue.add(moduleOffsetColorized, c);
		
        c.gridwidth = 1;
        c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 0; c.ipady = 0;
		c.weightx = 0; c.weighty = 0;
        
        c.gridx = 0; c.gridy = 0; add(moduleLabels[0],c);
        c.gridx = 1; c.gridy = 0; add(moduleLabels[1],c);
        c.gridx = 2; c.gridy = 0; add(moduleLabels[2],c);
        c.gridx = 0; c.gridy = 1; add(moduleLabels[3],c);
        c.gridx = 1; c.gridy = 1; add(moduleLabels[4],c);
        c.gridx = 2; c.gridy = 1; add(moduleLabels[5],c);
       
        
        c.gridwidth = 1; c.gridheight = 2;
	c.gridx = 3; c.gridy = 0; add(valuesDisplayed, c);
	c.gridx = 4; c.gridy = 0; add(colorizedValue, c);
		
        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
		dfInt = new DecimalFormat("#0");
		df.setRoundingMode(RoundingMode.DOWN);

        select = new ButtonGroup();
		select.add(moduleGainColorized);
		select.add(moduleOffsetColorized);

        offsetOffset = offsetScale = gainOffset = gainScale = 0;

        for (int i = 0; i < TelemetryGUI.NUM_BATT_MODULES; i++) {
            MessageHandler.get().subscribeData("autocal" + i, this);
			MessageHandler.get().subscribeData("rawautocal" + i, this);
        }
		
		System.out.println(moduleLabels[0].getSize());
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
		
        int module;
		
		if(message.getId().contains("raw")) {
			module = Integer.parseInt(message.getId().substring(10));
			
			moduleRef[module].setText("Ref: " + dfInt.format(message.get("ref")));
			moduleRefVolt[module].setText("Volt: " + df.format(message.get("refvolt")));
			
			moduleZero[module].setText("Zero: " + dfInt.format(message.get("zero")));
			moduleZeroVolt[module].setText("Volt: " + df.format(message.get("zerovolt")));
			
		} else {
			module = Integer.parseInt(message.getId().substring(7));
	        gains[module] = message.get("gain");
	        offsets[module] = message.get("offset");
			
			moduleGains[module].setText("Gain: " + df.format(gains[module]));
			moduleOffsets[module].setText("Offset: " + df.format(offsets[module]));
		}
		
        gainMin = Integer.MAX_VALUE;
        gainMax = Integer.MIN_VALUE;
        offsetMax = Integer.MIN_VALUE;
        offsetMin = Integer.MAX_VALUE;
        for (int x = 0; x < NUM_MODULES; x++) {
            if (gains[x] < gainMin) {
                gainMin = gains[x];
            }
            if (gains[x] > gainMax) {
                gainMax = gains[x];
            }
            if (offsets[x] > offsetMax) {
                offsetMax = offsets[x];
            }
            if (offsets[x] < offsetMin) {
                offsetMin = offsets[x];
            }
        }

        gainOffset = (gainMin + gainMax) / 2;
        if (gainMax - gainMin > 0.05) {
            gainScale = (gainMax == gainMin ? 0 : 2 / (gainMax - gainMin));
        } else {
            gainScale = 2 / 0.05;
        }

        offsetOffset = (offsetMin + offsetMax) / 2;
        if (offsetMax - offsetMin > 4) {
            offsetScale = (offsetMax == offsetMin ? 0 : 2 / (offsetMax - offsetMin));
        } else {
            offsetScale = 2.0 / 4;
        }

        if (moduleGainColorized.isSelected()) {
            Color bk;
            for (int x = 0; x < NUM_MODULES; x++) {
                float color = (float) (gainScale * (gains[x] - gainOffset));

                if (color < 0) {
                    color = Math.abs(color) * 0.75f;
                    bk = new Color(1 - color, 1 - color, 1);
                } else {
                    color = Math.abs(color) * 0.75f;
                    bk = new Color(1, 1 - color, 1 - color);
                }

                moduleLabels[x].setBackground(bk);
            }
        } else if (moduleOffsetColorized.isSelected()) {
            Color bk;
            for (int x = 0; x < NUM_MODULES; x++) {
                float color = (float) (offsetScale * (offsets[x] - offsetOffset));

                if (color < 0) {
                    color = Math.abs(color) * 0.75f;
                    bk = new Color(1 - color, 1 - color, 1);
                } else {
                    color = Math.abs(color) * 0.75f;
                    bk = new Color(1, 1 - color, 1 - color);
                }

                moduleLabels[x].setBackground(bk);
            }
        } else {
        }
    }

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		
		
		if(source == moduleGainEnabled) {
			showValues(moduleGains, moduleGainEnabled.isSelected());
		} else if (source == moduleOffsetEnabled) {
			showValues(moduleOffsets, moduleOffsetEnabled.isSelected());
		} else if (source == moduleRefEnabled) {
			showValues(moduleRef, moduleRefEnabled.isSelected());
		} else if (source == moduleRefVoltEnabled) {
			showValues(moduleRefVolt, moduleRefVoltEnabled.isSelected());
		} else if (source == moduleZeroEnabled) {
			showValues(moduleZero, moduleZeroEnabled.isSelected());
		} else if (source == moduleZeroVoltEnabled) {
			showValues(moduleZeroVolt, moduleZeroVoltEnabled.isSelected());
		}
	}
	
	private void showValues(JLabel labels[], boolean show) {
		/* Note: the code changes the color of the text rather than setting 
		 * visible or not visible to avoid redrawing and moving labels around
		 */

		for(JLabel l : labels) {
				if(!show) {
					//l.setForeground(l.getParent().getBackground());
					l.setForeground(new Color(0,0,0,0));
				}
				else {
					l.setForeground(Color.black);
				}
			}
	}
}