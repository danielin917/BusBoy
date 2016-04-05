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

public class MotorControllerStatusModule extends SolarPanel implements DataMessageSubscriber {
    private JLabel voltName, curName, pwrName;
    private JLabel rpmName, kphName, rmsCName, rmsBName;
    private JLabel emfRName, emfIName, voltRName, voltIName, curRName, curIName;
    private JLabel slipName, r15Name, r19Name, r33Name;
    private JLabel tempMotName, tempSinkName, tempBrdName;
    private JLabel odoName, aHName;
    private JLabel limName, errName, motName, txErrName, rxErrName;

    private JLabel voltVal, curVal, pwrVal;
    private JLabel rpmVal, kphVal, rmsCVal, rmsBVal;
    private JLabel emfRVal, emfIVal, voltRVal, voltIVal, curRVal, curIVal;
    private JLabel slipVal, r15Val, r19Val, r33Val;
    private JLabel tempMotVal, tempSinkVal, tempBrdVal;
    private JLabel odoVal, aHVal;
    private JLabel limVal, errVal, motVal, txErrVal, rxErrVal;

    private DecimalFormat df;
    private DecimalFormat df2;
    
    private final int motNumber;

    public MotorControllerStatusModule(int number) {
        motNumber = number;
    }
    
    @Override
    public void init() {
        if(motNumber == 1) {
            setBorder(new TitledBorder("Right Motor Controller (1)"));
        } else {
            setBorder(new TitledBorder("Left Motor Controller (2)"));
        }
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();        
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 10;
        c.ipady = 5;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;

        labels();
        layout(c);

        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df2 = new DecimalFormat("#0");
        
        subscribe();
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        switch (message.getId()) {
            case "motflag":
            case "motflag2":
                int lim_d = (int) (double) message.get("lim");
                String limText = "(" + message.get("lim") + ") ";
                if ((lim_d & 0b01000000) != 0) {
                    limText += "Temp";
                }
                if ((lim_d & 0b00100000) != 0) {
                    limText += "Bus Volt Low";
                }
                if ((lim_d & 0b00010000) != 0) {
                    limText += "Bus Volt High";
                }
                if ((lim_d & 0b00001000) != 0) {
                    limText += "Bus Current";
                }
                if ((lim_d & 0b00000100) != 0) {
                    limText += "Velocity";
                }
                if ((lim_d & 0b00000010) != 0) {
                    limText += "Motor Curr";
                }
                if ((lim_d & 0b00000001) != 0) {
                    limText += "Out Volt PWM";
                }
                limVal.setText(limText);
                
                String errorText;
                int error = (int) (double) message.get("err");
                if (error == 0) {
                    errVal.setText("OK (" + message.get("err") + ")");
                    errName.setForeground(Color.BLACK);
                    errVal.setForeground(Color.BLACK);
                } else {
                    errorText = "Error 1: (" + message.get("err") + ") ";
                    if ((error & 0b10000000) != 0) {
                        errorText += "Dsat, ";
                    }
                    if ((error & 0b01000000) != 0) {
                        errorText += "15Vrail, ";
                    }
                    if ((error & 0b00100000) != 0) {
                        errorText += "CfgReadErr, ";
                    }
                    if ((error & 0b00010000) != 0) {
                        errorText += "WatchdogRst, ";
                    }
                    if ((error & 0b00001000) != 0) {
                        errorText += "Hall, ";
                    }
                    if ((error & 0b00000100) != 0) {
                        errorText += "DC Overvolt, ";
                    }
                    if ((error & 0b00000010) != 0) {
                        errorText += "SW OverCurr, ";
                    }
                    if ((error & 0b00000001) != 0) {
                        errorText += "HW OverCurr ";
                    }
                    errVal.setText(errorText);
                    errName.setForeground(Color.RED);
                    errVal.setForeground(Color.RED);
                }
                motVal.setText(df2.format((int) (double) message.get("mot")));
                txErrVal.setText(df2.format((int) (double) message.get("txerr")));
                rxErrVal.setText(df2.format((int) (double) message.get("rxerr")));
                break;
                
            case "motbus":
            case "motbus2":
                voltVal.setText(df.format(message.get("volt")));
                curVal.setText(df.format(message.get("curr")));
                break;
                
            case "motvel":
            case "motvel2":
                rpmVal.setText(df.format(message.get("motvel")));
                kphVal.setText(df.format(message.get("vehvel") * 3.6));
                break;
                
            case "motphasecurrent":
            case "motphasecurrent2":
                rmsBVal.setText(df.format(message.get("phaseb")));
                rmsCVal.setText(df.format(message.get("phasec")));
                break;
                
            case "motvoltvector":
            case "motvoltvector2":
                voltRVal.setText(df.format(message.get("vq")));
                voltIVal.setText(df.format(message.get("vd")));
                break;
                
            case "motcurrentvector":
            case "motcurrentvector2":
                curRVal.setText(df.format(message.get("iq")));
                curIVal.setText(df.format(message.get("id")));
                break;
                
            case "motbackemf":
            case "motbackemf2":
                emfRVal.setText(df.format(message.get("bemfq")));
                emfIVal.setText(df.format(message.get("bemfd")));
                break;
                
            case "motrail0":
            case "motrail0_2":
                r15Val.setText(df.format(message.get("supply")));
                break;
                
            case "motrail1":
            case "motrail1_2":
                r19Val.setText(df.format(message.get("dsp")));
                r33Val.setText(df.format(message.get("fpga")));
                break;
                
            case "mottemp0":
            case "mottemp0_2":
                tempMotVal.setText(df.format(message.get("motor")));
                tempSinkVal.setText(df.format(message.get("heatsink")));
                break;
                
            case "mottemp1":
            case "mottemp1_2":
                tempBrdVal.setText(df.format(message.get("dsp")));
                break;
                
            case "motodo":
            case "motodo2":
                odoVal.setText(df.format(message.get("odometer")));
                aHVal.setText(df.format(message.get("amphours")));
                break;
                
            case "motslip":
            case "motslip2":
                slipVal.setText(df.format(message.get("slipspeed")));
                break;
                
            default:
                break;
        }
    }
    
    private void labels() {
        //Name Labels
        voltName = new JLabel("Voltage:");
        voltName.setPreferredSize(new Dimension(100, 15));
        curName = new JLabel("Current:");
        curName.setPreferredSize(new Dimension(100, 15));
        pwrName = new JLabel("Power out:");
        pwrName.setPreferredSize(new Dimension(100, 15));
    
        rpmName = new JLabel("RPM:");
        rpmName.setPreferredSize(new Dimension(100, 15));
        kphName = new JLabel("KPH:");
        kphName.setPreferredSize(new Dimension(100, 15));
        rmsCName = new JLabel("Phase C Cur:");
        rmsCName.setPreferredSize(new Dimension(100, 15));
        rmsBName = new JLabel("Phase B Cur:");
        rmsBName.setPreferredSize(new Dimension(100, 15));

        emfRName = new JLabel("Back EMF (Real):");
        emfRName.setPreferredSize(new Dimension(100, 15));
        emfIName = new JLabel("Back EMF (Imag):");
        emfIName.setPreferredSize(new Dimension(100, 15));
        voltRName = new JLabel("Volt (Real):");
        voltRName.setPreferredSize(new Dimension(100, 15));
        voltIName = new JLabel("Volt (Imag):");
        voltIName.setPreferredSize(new Dimension(100, 15));
        curRName = new JLabel("Cur (Real):");
        curRName.setPreferredSize(new Dimension(100, 15));
        curIName = new JLabel("Cur (Imag):");
        curIName.setPreferredSize(new Dimension(100, 15));
    
        slipName = new JLabel("Slip Speed:");
        slipName.setPreferredSize(new Dimension(100, 15));
        r15Name = new JLabel("15V Rail:");
        r15Name.setPreferredSize(new Dimension(100, 15));
        r19Name = new JLabel("1.9V Rail:");
        r19Name.setPreferredSize(new Dimension(100, 15));
        r33Name = new JLabel("3.3V Rail:");
        r33Name.setPreferredSize(new Dimension(100, 15));
    
        tempMotName = new JLabel("Motor Temp:");
        tempMotName.setPreferredSize(new Dimension(100, 15));
        tempSinkName = new JLabel("Heatsink Temp:");
        tempSinkName.setPreferredSize(new Dimension(100, 15));
        tempBrdName = new JLabel("Board Temp:");
        tempBrdName.setPreferredSize(new Dimension(100, 15));

        odoName = new JLabel("Odometer:");
        odoName.setPreferredSize(new Dimension(100, 15));
        aHName = new JLabel("Amp Hours:");
        aHName.setPreferredSize(new Dimension(100, 15));
        
        limName = new JLabel("Limit:");
        limName.setPreferredSize(new Dimension(100, 15));
        errName = new JLabel("Error:");
        errName.setPreferredSize(new Dimension(100, 15));
        motName = new JLabel("Active Motor:");
        motName.setPreferredSize(new Dimension(100, 15));
        txErrName = new JLabel("TX Err:");
        txErrName.setPreferredSize(new Dimension(100, 15));
        rxErrName = new JLabel("RX Err:");
        rxErrName.setPreferredSize(new Dimension(100, 15));
        
        //Value Labels
        voltVal = new JLabel("N/A");
        voltVal.setPreferredSize(new Dimension(100, 15));
        curVal = new JLabel("N/A");
        curVal.setPreferredSize(new Dimension(100, 15));
        pwrVal = new JLabel("N/A");
        pwrVal.setPreferredSize(new Dimension(100, 15));
    
        rpmVal = new JLabel("N/A");
        rpmVal.setPreferredSize(new Dimension(100, 15));
        kphVal = new JLabel("N/A");
        kphVal.setPreferredSize(new Dimension(100, 15));
        rmsCVal = new JLabel("N/A");
        rmsCVal.setPreferredSize(new Dimension(100, 15));
        rmsBVal = new JLabel("N/A");
        rmsBVal.setPreferredSize(new Dimension(100, 15));

        emfRVal = new JLabel("N/A");
        emfRVal.setPreferredSize(new Dimension(100, 15));
        emfIVal = new JLabel("N/A");
        emfIVal.setPreferredSize(new Dimension(100, 15));
        voltRVal = new JLabel("N/A");
        voltRVal.setPreferredSize(new Dimension(100, 15));
        voltIVal = new JLabel("N/A");
        voltIVal.setPreferredSize(new Dimension(100, 15));
        curRVal = new JLabel("N/A");
        curRVal.setPreferredSize(new Dimension(100, 15));
        curIVal = new JLabel("N/A");
        curIVal.setPreferredSize(new Dimension(100, 15));
    
        slipVal = new JLabel("N/A");
        slipVal.setPreferredSize(new Dimension(100, 15));
        r15Val = new JLabel("N/A");
        r15Val.setPreferredSize(new Dimension(100, 15));
        r19Val = new JLabel("N/A");
        r19Val.setPreferredSize(new Dimension(100, 15));
        r33Val = new JLabel("N/A");
        r33Val.setPreferredSize(new Dimension(100, 15));
    
        tempMotVal = new JLabel("N/A");
        tempMotVal.setPreferredSize(new Dimension(100, 15));
        tempSinkVal = new JLabel("N/A");
        tempSinkVal.setPreferredSize(new Dimension(100, 15));
        tempBrdVal = new JLabel("N/A");
        tempBrdVal.setPreferredSize(new Dimension(100, 15));

        odoVal = new JLabel("N/A");
        odoVal.setPreferredSize(new Dimension(100, 15));
        aHVal = new JLabel("N/A");
        aHVal.setPreferredSize(new Dimension(100, 15));
        
        limVal = new JLabel("N/A");
        limVal.setPreferredSize(new Dimension(100, 15));
        errVal = new JLabel("N/A");
        errVal.setPreferredSize(new Dimension(100, 15));
        motVal = new JLabel("N/A");
        motVal.setPreferredSize(new Dimension(100, 15));
        txErrVal = new JLabel("N/A");
        txErrVal.setPreferredSize(new Dimension(100, 15));
        rxErrVal = new JLabel("N/A");
        rxErrVal.setPreferredSize(new Dimension(100, 15));
    }
    
    private void layout(GridBagConstraints c) {
        c.gridx = 0;
        c.gridy = 0;
        add(motName, c);
        c.gridx = 1;
        add(motVal, c);
        c.gridx = 2;
        add(pwrName, c);
        c.gridx = 3;
        add(pwrVal, c);
        c.gridx = 4;
        add(kphName, c);
        c.gridx = 5;
        add(kphVal, c);
        
        c.gridx = 0;
        c.gridy = 1;
        add(errName, c);
        c.gridx = 1;
        c.gridwidth = 3;
        add(errVal, c);
        c.gridx = 4;
        c.gridwidth = 1;
        add(txErrName, c);
        c.gridx = 5;
        add(txErrVal, c);
        
        c.gridx = 0;
        c.gridy = 2;
        add(limName, c);
        c.gridx = 1;
        c.gridwidth = 3;
        add(limVal, c);
        c.gridx = 4;
        c.gridwidth = 1;
        add(rxErrName, c);
        c.gridx = 5;
        add(rxErrVal, c);
        
        c.gridx = 0;
        c.gridy = 3;
        add(voltName, c);
        c.gridx = 1;
        add(voltVal, c);
        c.gridx = 2;
        add(curName, c);
        c.gridx = 3;
        add(curVal, c);
        c.gridx = 4;
        add(aHName, c);
        c.gridx = 5;
        add(aHVal, c);
        
        c.gridx = 0;
        c.gridy = 4;
        add(r15Name, c);
        c.gridx = 1;
        add(r15Val, c);
        c.gridx = 2;
        add(r33Name, c);
        c.gridx = 3;
        add(r33Val, c);
        c.gridx = 4;
        add(r19Name, c);
        c.gridx = 5;
        add(r19Val, c);
        
        c.gridx = 0;
        c.gridy = 5;
        add(tempMotName, c);
        c.gridx = 1;
        add(tempMotVal, c);
        c.gridx = 2;
        add(tempBrdName, c);
        c.gridx = 3;
        add(tempBrdVal, c);
        c.gridx = 4;
        add(tempSinkName, c);
        c.gridx = 5;
        add(tempSinkVal, c);
        
        c.gridx = 0;
        c.gridy = 6;
        add(emfRName, c);
        c.gridx = 1;
        add(emfRVal, c);
        c.gridx = 2;
        add(emfIName, c);
        c.gridx = 3;
        add(emfIVal, c);
        c.gridx = 4;
        add(rpmName, c);
        c.gridx = 5;
        add(rpmVal, c);
        
        c.gridx = 0;
        c.gridy = 7;
        add(voltRName, c);
        c.gridx = 1;
        add(voltRVal, c);
        c.gridx = 2;
        add(voltIName, c);
        c.gridx = 3;
        add(voltIVal, c);
        c.gridx = 4;
        add(slipName, c);
        c.gridx = 5;
        add(slipVal, c);
        
        c.gridx = 0;
        c.gridy = 8;
        add(curRName, c);
        c.gridx = 1;
        add(curRVal, c);
        c.gridx = 2;
        add(curIName, c);
        c.gridx = 3;
        add(curIVal, c);
        c.gridx = 4;
        add(odoName, c);
        c.gridx = 5;
        add(odoVal, c);
        
        c.gridx = 0;
        c.gridy = 9;
        add(rmsBName, c);
        c.gridx = 1;
        add(rmsBVal, c);
        c.gridx = 2;
        add(rmsCName, c);
        c.gridx = 3;
        add(rmsCVal, c);
    }
    
    private void subscribe() {
        if(motNumber == 1) {
            MessageHandler.get().subscribeData("motflag", this);
            MessageHandler.get().subscribeData("motbus", this);
            MessageHandler.get().subscribeData("motvel", this);
            MessageHandler.get().subscribeData("motphasecurrent", this);
            MessageHandler.get().subscribeData("motvoltvector", this);
            MessageHandler.get().subscribeData("motcurrentvector", this);
            MessageHandler.get().subscribeData("motbackemf", this);
            MessageHandler.get().subscribeData("motrail0", this);
            MessageHandler.get().subscribeData("motrail1", this);
            MessageHandler.get().subscribeData("mottemp0", this);
            MessageHandler.get().subscribeData("mottemp1", this);
            MessageHandler.get().subscribeData("motodo", this);
            MessageHandler.get().subscribeData("motslip", this);
        }
        else {            
            MessageHandler.get().subscribeData("motflag2", this);
            MessageHandler.get().subscribeData("motbus2", this);
            MessageHandler.get().subscribeData("motvel2", this);
            MessageHandler.get().subscribeData("motphasecurrent2", this);
            MessageHandler.get().subscribeData("motvoltvector2", this);
            MessageHandler.get().subscribeData("motcurrentvector2", this);
            MessageHandler.get().subscribeData("motbackemf2", this);
            MessageHandler.get().subscribeData("motrail0_2", this);
            MessageHandler.get().subscribeData("motrail1_2", this);
            MessageHandler.get().subscribeData("mottemp0_2", this);
            MessageHandler.get().subscribeData("mottemp1_2", this);
            MessageHandler.get().subscribeData("motodo2", this);
            MessageHandler.get().subscribeData("motslip2", this);
        }
    }
}
