package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
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

public class  MotorDetailPanel extends SolarPanel implements DataMessageSubscriber {

    private JLabel motorTempName, motorTemp;
    private JLabel dspTempName, dspTemp;
    private JLabel phaseCTempName, phaseCTemp;
    private DecimalFormat df;

    public MotorDetailPanel() {
        super();
        // initialize panel
        setBorder(new TitledBorder("WS22 Temp"));
        setPreferredSize(new Dimension(150, 170));
        setLayout(new GridBagLayout());

        // Initialize value labels
        motorTempName = new JLabel("Stator: ");
        motorTempName.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        dspTempName = new JLabel("DSP: ");
        dspTempName.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        /*
         * phaseATempName = new JLabel("Phase A: ");
         * phaseATempName.setAlignmentX(JLabel.RIGHT_ALIGNMENT); phaseBTempName
         * = new JLabel("Phase B: ");
                 phaseBTempName.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
         */
        phaseCTempName = new JLabel("IPM Phase C: ");
        phaseCTempName.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

        motorTemp = new JLabel("N/A");
        motorTemp.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        dspTemp = new JLabel("N/A");
        dspTemp.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        /*
         * phaseATemp = new JLabel("N/A");
         * phaseATemp.setAlignmentX(JLabel.LEFT_ALIGNMENT); phaseBTemp = new
         * JLabel("N/A");
		 phaseBTemp.setAlignmentX(JLabel.LEFT_ALIGNMENT);
         */
        phaseCTemp = new JLabel("N/A");
        phaseCTemp.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();

        // Add labels

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        add(motorTempName, c);
        c.gridx = 1;
        c.gridy = 0;
        add(motorTemp, c);

        c.gridx = 0;
        c.gridy = 1;
        add(dspTempName, c);
        c.gridx = 1;
        c.gridy = 1;
        add(dspTemp, c);

        /*
         * c.gridx = 0;	c.gridy = 2; add(phaseATempName, c); c.gridx = 1;
         * c.gridy = 2; add(phaseATemp, c);
         *
         * c.gridx = 0;	c.gridy = 3; add(phaseBTempName, c); c.gridx = 1;
         * c.gridy = 3; add(phaseBTemp, c);
         */

        c.gridx = 0;
        c.gridy = 2;
        add(phaseCTempName, c);
        c.gridx = 1;
        c.gridy = 2;
        add(phaseCTemp, c);

        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

    }

    @Override
    public void init() {
        MessageHandler.get().subscribeData("mottemp0", this);
        MessageHandler.get().subscribeData("mottemp1", this);
        MessageHandler.get().subscribeData("mottemp2", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
		switch (msg.getId()) {
			case "mottemp0":
				motorTemp.setText(df.format(msg.get("motor")) + " C");
				phaseCTemp.setText(df.format(msg.get("phasec")) + " C");
				break;
			case "mottemp1":
				dspTemp.setText(df.format(msg.get("dsp")) + " C");
				//phaseBTemp.setText(df.format(msg.get("phaseb"))+" C");
				break;
			case "mottemp2":
				//phaseATemp.setText(df.format(msg.get("phasea"))+" C");
				break;
			default:
				break;
		}
    }

    @Override
    public String toString() {
        return "Motor detail panel";
    }
}
