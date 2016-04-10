package solarcar.gui.modules;


import com.sun.prism.paint.Color;
import solarcar.vdcListener.MessageHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;
import solarcar.util.filters.averageStream;
import solarcar.util.filters.endpointStream;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;

public class  Statistometer extends SolarPanel implements Runnable, ActionListener {
    private Thread thread;
    private int updateFreq;
    private long sqlTimeout;
    
    JLabel realLabel;
    JLabel simulatedLabel;
    JLabel pinLabelReal;
    JLabel poutLabelReal;
    JLabel pinLabelSimulated;
    JLabel poutLabelSimulated;
    JLabel SOCLabelReal;
    JLabel SOCLabelSimulated;
    JLabel radLabelReal;
    JLabel radLabelSimulated;
    JLabel resetTimeLabel;
    JLabel windLabelReal;
    JLabel windLabelSimulated;

    private PowerInGraph StatPowerInGraph;
    private MotorPowerGraph StatPowerOutGraph;
    private SOCGraph StatSOCGraph;
    private SolarRadiationGraph StatSolarRadiationGraph;
    private WindSpeedGraph StatWindSpeedGraph;
     
    private Double startTime, endTime;
    
    JButton resetAvgsButton;
        
    DecimalFormat df;
    
    public Statistometer(PowerInGraph inputPowerInGraph, MotorPowerGraph inputPowerOutGraph, SOCGraph inputSOCGraph, SolarRadiationGraph inputSolarRadiationGraph, WindSpeedGraph inputWindSpeedGraph){
        StatPowerInGraph = inputPowerInGraph;
        StatPowerOutGraph = inputPowerOutGraph;
        StatSOCGraph = inputSOCGraph;
        StatSolarRadiationGraph = inputSolarRadiationGraph;
        StatWindSpeedGraph = inputWindSpeedGraph;
    }

    public void initLabel(JLabel label, GridBagConstraints c)
    {
        label.setFont(new Font(label.getFont().getFontName(),
                label.getFont().getStyle(), 20));
        label.setPreferredSize(new Dimension(120, 25));
        add(label,c);
    }
    @Override
    public void init() {
        double range=3600;//seconds
        
        setBorder(new TitledBorder("Statistics"));
        setPreferredSize(new Dimension(280, 220));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        startTime = endTime = (double)System.currentTimeMillis();
        
        realLabel = new JLabel("<html><font size=3>Real Data</font></html>");
        c.gridx = 0; c.gridy = 0;
        initLabel(realLabel,c);
        simulatedLabel = new JLabel("<html><font size = 3>Simulated Data </font></html>");
        c.gridx = 1; c.gridy = 0;
        initLabel(simulatedLabel,c);
        SOCLabelReal = new JLabel("<html>N/A<font size=2> SOC Real</font></html>");
        c.gridx = 0; c.gridy = 1;
        initLabel(SOCLabelReal,c);
        SOCLabelSimulated = new JLabel("<html>N/A<font size=2> SOC Simulated</font></html>");
        c.gridx = 1; c.gridy = 1;
        initLabel(SOCLabelSimulated,c);
        
        pinLabelReal = new JLabel("<html>N/A<font size=2> PIn Real</font></html>");
        c.gridx = 0; c.gridy = 2;
        initLabel(pinLabelReal,c);
        pinLabelSimulated = new JLabel("<html>N/A<font size=2> PIn Simulated</font></html>");
        c.gridx = 1; c.gridy = 2;
        initLabel(pinLabelSimulated,c);
        
        poutLabelReal = new JLabel("<html>N/A<font size=2> POut Real</font></html>");
        c.gridx = 0; c.gridy = 3;
        initLabel(poutLabelReal,c);
        poutLabelSimulated = new JLabel("<html>N/A<font size=2> POut Simulated</font></html>");
        c.gridx = 1; c.gridy = 3;
        initLabel(poutLabelSimulated,c);
        
        radLabelReal = new JLabel("<html>N/A<font size=2> Rad Real</font></html>");
        c.gridx = 0; c.gridy = 4;
        initLabel(radLabelReal,c);
        radLabelSimulated = new JLabel("<html>N/A<font size=2> Rad Simulated</font></html>");
        c.gridx = 1; c.gridy = 4;
        initLabel(radLabelSimulated,c);
        
        windLabelReal = new JLabel("<html>N/A<font size=2> Wind Real</font></html>");
        c.gridx = 0; c.gridy = 5;
        initLabel(windLabelReal, c);
        //windLabelSimulated = new JLabel("<html>N/A<font size=2> Wind</font></html>");
        //c.gridx = 1; c.gridy = 5;
        //initLabel(windLabelSimulated, c);
        
        resetAvgsButton = new JButton("Reset Averages");
        resetAvgsButton.addActionListener(this);
        resetAvgsButton.setActionCommand("Reset");
        c.gridx = 0; c.gridy = 6;
        add(resetAvgsButton,c);
        
        resetTimeLabel = new JLabel("<html>N/A<font size=2> Time Since Update</font></html>");
        c.gridx = 1; c.gridy = 5;
        initLabel(resetTimeLabel, c);
        
        updateFreq = 1;

        thread = new Thread(this);
        thread.start();
        
        df = new DecimalFormat("#0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);

        //soc
        //pin
    }
    
    public void updateLabels() {
        //soc
        try {
            endTime = (double)System.currentTimeMillis();
            resetTimeLabel.setText("<html><font size=4>" + df.format((endTime-startTime)/1000) + " </html>");
            SOCLabelReal.setText("<html><font size=4> " + df.format(StatSOCGraph.SOCStreamReal.get()) + "</font><font size=3>SOC</font></html>");
            pinLabelReal.setText("<html><font size=4>" + df.format(StatPowerInGraph.pinStreamReal.get()) + " </font><font size=3>PIn</font></html>");
            poutLabelReal.setText("<html><font size=4>" + df.format(StatPowerOutGraph.poutStreamReal.get()) + " </font><font size=3>POut</font></html>");
            radLabelReal.setText("<html><font size=4>" + df.format(StatSolarRadiationGraph.actualRadiationStream.get()) + " </font><font size=3>Rad</font></html>");
            windLabelReal.setText("<html><font size=4>" + df.format(StatWindSpeedGraph.windSpeedStreamReal.get()) + " </font><font size=3>WSpd</font></html>");
            
            SOCLabelSimulated.setText("<html><font size=4>" + df.format(StatSOCGraph.SOCStreamSimulated.get()) + " </font><font size=3>SOC</font></html>");
            pinLabelSimulated.setText("<html><font size=4>" + df.format(StatPowerInGraph.pinStreamSimulated.get()) + " </font><font size=3>PIn</font></html>");
            poutLabelSimulated.setText("<html><font size=4>" + df.format(StatPowerOutGraph.poutStreamSimulated.get()) + " </font><font size=3>POut</font></html>");
            radLabelSimulated.setText("<html><font size=4>" + df.format(StatSolarRadiationGraph.predictedRadiationStream.get()) + " </font><font size=3>Rad</font></html>");
            //windLabelSimulated.setText("<html><font size=4>" + df.format(StatWindSpeedGraph.windSpeedStreamSimulated.get()) + " </font><font size=3>Wind</font></html>");
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
        
    }
    
    @Override
    public void run(){
        System.out.println("Trying to get average data");
        Statement st = null;
        while (true) {
            updateLabels();
        }
    }
    @Override
    public void actionPerformed(ActionEvent v) {
        if (v.getActionCommand().equals("Reset")) {
            startTime = (double)System.currentTimeMillis();
            try{
                StatPowerInGraph.graph.resetLine(3);
                StatPowerInGraph.graph.addPoint(3,-10000);
                StatPowerInGraph.graph.addPoint(3,100000);
                StatSOCGraph.graph.resetLine(3);
                StatSOCGraph.graph.addPoint(3,-10000);
                StatSOCGraph.graph.addPoint(3,100000);
                StatPowerOutGraph.graph.resetLine(3);
                StatPowerOutGraph.graph.addPoint(3,-10000);
                StatPowerOutGraph.graph.addPoint(3,100000);
                StatSolarRadiationGraph.graph.resetLine(2);
                StatSolarRadiationGraph.graph.addPoint(2,-10000);
                StatSolarRadiationGraph.graph.addPoint(2,100000);
                StatWindSpeedGraph.graph.resetLine(2);
                StatWindSpeedGraph.graph.addPoint(2,-10000);
                StatWindSpeedGraph.graph.addPoint(2,100000);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            StatPowerInGraph.pinStreamReal.reset();
            StatPowerInGraph.pinStreamSimulated.reset();
            StatPowerOutGraph.poutStreamReal.reset();
            StatPowerOutGraph.poutStreamSimulated.reset();
            StatSOCGraph.SOCStreamReal.reset();
            StatSOCGraph.SOCStreamSimulated.reset();
            StatSolarRadiationGraph.actualRadiationStream.reset();
            StatSolarRadiationGraph.predictedRadiationStream.reset();
            StatWindSpeedGraph.windSpeedStreamReal.reset();
            StatWindSpeedGraph.windSpeedStreamSimulated.reset();
        }
    }
 }
