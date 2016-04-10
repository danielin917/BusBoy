package solarcar.gui.tabs;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import solarcar.gui.TelemetryGUI;

public class  OptionsTab extends JPanel implements ItemListener, ChangeListener, ActionListener {

    JCheckBox portOption;
    JCheckBox quantumCursor;
    JSpinner port;
    SpinnerNumberModel portSpinner;
    JPanel filler;
    JTextField databaseName;
    JLabel databaseLabel;
    JButton databaseSet;
    JLabel aneLabel;
    JRadioButton windSpeed;
    JRadioButton airSpeed;
    JRadioButton windDir;
    JRadioButton airDir;
    ButtonGroup aneSpeed;
    ButtonGroup aneDir;
    JSpinner socOffsetSpinner;
    SpinnerNumberModel socOffsetModel;
    JLabel socOffsetLabel;
    JLabel speedOptLabel;
    JRadioButton opt_mph;
    JRadioButton opt_kph;
    JRadioButton opt_ms;
    ButtonGroup opt_speed;
    JButton databaseReconnect;
    JLabel  packOptLabel;
    JRadioButton    opt_main;
    JRadioButton    opt_full;
    ButtonGroup    opt_pack;

    static private OptionsTab tab = null;

    static public OptionsTab get() {
        if (tab == null) {
            tab = new OptionsTab();
			tab.portOption.addItemListener(tab);
			tab.quantumCursor.addItemListener(tab);
			tab.portSpinner.addChangeListener(tab);
			tab.databaseName.addActionListener(tab);
			tab.databaseSet.addActionListener(tab);
			tab.windSpeed.addActionListener(tab);
			tab.airSpeed.addActionListener(tab);
			tab.windDir.addActionListener(tab);
			tab.airDir.addActionListener(tab);
			tab.databaseReconnect.addActionListener(tab);
        }
        return tab;
    }

    private OptionsTab() {
        //this.setLayout(new GridBagLayout());
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        portOption = new JCheckBox("Listen on defined port");
        portOption.setSelected(false);

        quantumCursor = new JCheckBox("Quantum Cursor");
        quantumCursor.setSelected(false);

        //portSpinner = new SpinnerNumberModel((new Random()).nextInt(10000) + 55000, 1024, 65535, 1 );
        portSpinner = new SpinnerNumberModel(9000, 1024, 65535, 1);
        
        port = new JSpinner(portSpinner);
        port.setEnabled(portOption.isSelected());

        databaseLabel = new JLabel("Database: ");
        databaseName = new JTextField(TelemetryGUI.db, 11);
        databaseName.setActionCommand("change db");
        
        databaseSet = new JButton("Set");
        databaseSet.setMnemonic(KeyEvent.VK_S);
        databaseSet.setActionCommand("change db");
        
        aneLabel = new JLabel("Anemomter visual: ");

        windSpeed = new JRadioButton("Wind speed");
        windSpeed.setActionCommand("wind speed");
        airSpeed = new JRadioButton("Air speed");
        airSpeed.setActionCommand("air speed");
        aneSpeed = new ButtonGroup();
        aneSpeed.add(airSpeed);
        aneSpeed.add(windSpeed);
        windSpeed.setSelected(true);

        windDir = new JRadioButton("Wind heading");
        windDir.setActionCommand("wind heading");
        airDir = new JRadioButton("Air heading");
        airDir.setActionCommand("air heading");
        aneDir = new ButtonGroup();
        aneDir.add(windDir);
        aneDir.add(airDir);
        windDir.setSelected(true);

        socOffsetModel = new SpinnerNumberModel(0.0, -10000.0, 10000.0, 1.0);
        socOffsetSpinner = new JSpinner(socOffsetModel);
        socOffsetLabel = new JLabel("SOC offset: ");
        
        speedOptLabel = new JLabel("Speed Units: ");
        opt_mph = new JRadioButton("MPH");
        opt_kph = new JRadioButton("KPH");
        opt_ms = new JRadioButton("M/S");
        opt_speed = new ButtonGroup();
        opt_speed.add(opt_mph);
        opt_speed.add(opt_kph);
        opt_speed.add(opt_ms);
        opt_kph.setSelected(true);
        
        packOptLabel = new JLabel("Pack Config: ");
        opt_main = new JRadioButton("Main");
        opt_full = new JRadioButton("Full");
        opt_pack = new ButtonGroup();
        opt_pack.add(opt_main);
        opt_pack.add(opt_full);
        opt_main.setSelected(true);

        databaseReconnect = new JButton("Reconnect to database");
        databaseReconnect.setActionCommand("reconnect db");

        filler = new JPanel();
        filler.setLayout(new GridBagLayout());
        this.add(filler);

        GridBagConstraints c = new GridBagConstraints();
        
        c.gridy = 1;
        filler.add(quantumCursor, c);

        c.gridx = 0;
        c.gridy = 3;
        filler.add(databaseLabel, c);

        c.gridx = 1;
        filler.add(databaseName, c);
        c.gridx = 2;
        filler.add(databaseSet, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        filler.add(databaseReconnect, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        filler.add(aneLabel, c);

        c.gridx = 0;
        c.gridy = 6;
        filler.add(windDir, c);
        c.gridx = 1;
        c.gridy = 6;
        filler.add(airDir, c);

        c.gridx = 0;
        c.gridy = 7;
        filler.add(windSpeed, c);
        c.gridx = 1;
        c.gridy = 7;
        filler.add(airSpeed, c);

        c.gridx = 0;
        c.gridy = 8;
        filler.add(socOffsetLabel, c);
        c.gridx = 1;
        c.gridy = 8;
        filler.add(socOffsetSpinner, c);

        c.gridx = 0;
        c.gridy = 10;
        filler.add(speedOptLabel, c);
        c.gridx = 1;
        c.gridy = 10;
        filler.add(opt_mph, c);
        c.gridx = 2;
        c.gridy = 10;
        filler.add(opt_kph, c);
        c.gridx = 3;
        c.gridy = 10;
        filler.add(opt_ms, c);
        
        c.gridx = 0;
        c.gridy = 12;
        filler.add(packOptLabel, c);
        c.gridx = 1;
        c.gridy = 12;
        filler.add(opt_main, c);
        c.gridx = 2;
        c.gridy = 12;
        filler.add(opt_full, c);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == portOption) {
            System.out.println("port option: " + portOption.isSelected());
            port.setEnabled(portOption.isSelected());
        } else if (source == quantumCursor) {
            if (quantumCursor.isSelected()) {
                TelemetryGUI.getFrame().setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("resources" + TelemetryGUI.fileSep() + "logo-400x1857-yellow.png"), new Point(0, 0), "M"));
            } else {
                TelemetryGUI.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source == portSpinner) {
           // TelemetryGUI.createStaticSocket(((Integer) portSpinner.getNumber()).intValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "change db":
                TelemetryGUI.db = databaseName.getText();
                break;
            case "reconnect db":
                //TODO: This is a really ugly way to do reconnect to SQL...
                Thread sqlThread = new Thread() {
                    @Override
                    public void run() {
                        TelemetryGUI.initSQLConn();
                    }
                };
                sqlThread.start();
                break;
			default:
				break;
        }
    }

    public boolean useWindSpeed() {
        return windSpeed.isSelected();
    }

    public boolean useWindDir() {
        return windDir.isSelected();
    }

    public double SOCOffset() {
        return (socOffsetModel.getNumber()).doubleValue();
    }

    public boolean opt_mph() {
        return opt_mph.isSelected();
    }

    public boolean opt_kph() {
        return opt_kph.isSelected();
    }

    public boolean opt_ms() {
        return opt_ms.isSelected();
    }
    
    public boolean opt_main() {
        return opt_main.isSelected();
    }

    public boolean opt_full() {
        return opt_full.isSelected();
    }
	
	@Override
	public String toString() {
		return "Options tab";
	}
}