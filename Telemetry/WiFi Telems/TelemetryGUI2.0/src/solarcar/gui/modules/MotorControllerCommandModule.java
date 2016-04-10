package solarcar.gui.modules;


import solarcar.vdcListener.VDCConn;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  MotorControllerCommandModule extends SolarPanel implements DataMessageSubscriber, ActionListener {

    JButton motCtrlRstButton;
    JTextField motorSelectField;
    JButton sendMotToBlueButton;
    JButton sendBMSRstToBlueButton;
    JButton openRelayButton;
    DecimalFormat df, df2;

    @Override
    public void init() {
        setBorder(new TitledBorder("Motor Cmd"));
        setPreferredSize(new Dimension(150, 150));
        setLayout(new FlowLayout(FlowLayout.LEADING));

        motCtrlRstButton = new JButton("Reset Motor");
        motCtrlRstButton.addActionListener(this);

        sendBMSRstToBlueButton = new JButton("Reset BMS");
        sendBMSRstToBlueButton.addActionListener(this);
        
        motorSelectField = new JTextField(3);
        sendMotToBlueButton = new JButton("Set Motor");
        sendMotToBlueButton.addActionListener(this);
        
        openRelayButton = new JButton("Open Relay");
        openRelayButton.addActionListener(this);

        add(motCtrlRstButton);
        add(sendBMSRstToBlueButton);
        add(motorSelectField);
        add(sendMotToBlueButton);
        add(openRelayButton);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Reset Motor")) {
            VDCConn.get().sendMessage("can addr='0x223' data='0x0' len='0' priority='0' ext='0' rem='0'");
        } else if(e.getActionCommand().equals("Reset BMS"))
        {
            System.out.println("BMS Reset");
            VDCConn.get().sendMessage("can addr='0x229' data='0x0' len='0' priority='0' ext='0' rem='0'");
        } else if(e.getActionCommand().equals("Open Relay"))
        {
            int option = JOptionPane.showConfirmDialog(this, "Warning: Opening the relay can be dangerous.\nProceed to open relay?");
            if(option == 0)
                VDCConn.get().sendMessage("can addr='0x236' data='0x0' len='0' priority='0' ext='0' rem='0'");
        }
        else if(e.getActionCommand().equals("Set Motor"))
        VDCConn.get().sendMessage("can addr='0x24F' data='0x4143544d4f540009 len='8' priority='0' ext='0' rem='0'");
    }
}