package solarcar.gui.modules;


import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.SolarPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

public class  AlarmModule extends SolarPanel implements DataMessageSubscriber, ItemListener {

    JCheckBox enableOvervolt;
    JCheckBox enableOvercurrent;
    JCheckBox enableUndervolt;
    JCheckBox enableBattOvertemp;
    JCheckBox enableHSOvertemp;
    JCheckBox enableMotCtrlErr;
    JCheckBox enablePan;
    JCheckBox enableMayday;
    AudioStream asOvervolt;
    AudioStream asOvercurrent;
    AudioStream asUndervolt;
    AudioStream asBattOvertemp;
    AudioStream asHSOvertemp;
    AudioStream asMotCtrlErr;
    AudioStream asPan;
    AudioStream asMayday;
    ContinuousAudioDataStream casOvervolt;
    ContinuousAudioDataStream casOvercurrent;
    ContinuousAudioDataStream casUndervolt;
    ContinuousAudioDataStream casBattOvertemp;
    ContinuousAudioDataStream casHSOvertemp;
    ContinuousAudioDataStream casMotCtrlErr;
    ContinuousAudioDataStream casPan;
    ContinuousAudioDataStream casMayday;
    boolean playingOvervoltAlarm;
    boolean playingOvercurrentAlarm;
    boolean playingUndervoltAlarm;
    boolean playingBattOvertempAlarm;
    boolean playingHSOvertempAlarm;
    boolean playingMotCtrlErrAlarm;
    boolean playingPanAlarm;
    boolean playingMaydayAlarm;

    @Override
    public void init() {

        System.out.println("Initializing alarms");

        setBorder(new TitledBorder("Alarms"));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        setPreferredSize(new Dimension(130, 170));

        enableOvervolt = new JCheckBox("batt overvolt");
        enableOvercurrent = new JCheckBox("batt overcurrent");
        enableUndervolt = new JCheckBox("batt undervolt");
        enableBattOvertemp = new JCheckBox("batt overtemp");
        enableHSOvertemp = new JCheckBox("ws overtemp");
        enableMotCtrlErr = new JCheckBox("motCtrlErr");
        enablePan = new JCheckBox("squawk:pan");
        enableMayday = new JCheckBox("squawk:mayday");

        enableOvervolt.setSelected(true);
        enableOvercurrent.setSelected(true);
        enableUndervolt.setSelected(true);
        enableBattOvertemp.setSelected(true);
        enableHSOvertemp.setSelected(true);
        enableMotCtrlErr.setSelected(true);
        enablePan.setSelected(true);
        enableMayday.setSelected(true);

        enableOvervolt.addItemListener(this);
        enableOvercurrent.addItemListener(this);
        enableUndervolt.addItemListener(this);
        enableBattOvertemp.addItemListener(this);
        enableHSOvertemp.addItemListener(this);
        enableMotCtrlErr.addItemListener(this);
        enablePan.addItemListener(this);
        enableMayday.addItemListener(this);

        add(enableOvervolt);
        add(enableOvercurrent);
        add(enableUndervolt);
        add(enableBattOvertemp);
        add(enableHSOvertemp);
        add(enableMotCtrlErr);
        //add(enablePan);
        //add(enableMayday);

        try {

            asOvervolt = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "overvolt_alarm.wav"));
            asOvercurrent = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "overvolt_alarm.wav"));
            asUndervolt = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "undervolt_alarm.wav"));
            asBattOvertemp = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "batt_overtemp_alarm.wav"));
            asHSOvertemp = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "hs_overtemp_alarm.wav"));
            asMotCtrlErr = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "mot_ctrl_err_alarm.wav"));
            asPan = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "panpan_alarm.wav"));
            asMayday = new AudioStream(new FileInputStream("resources" + TelemetryGUI.fileSep() + "mayday_alarm.wav"));

            casOvervolt = new ContinuousAudioDataStream(asOvervolt.getData());
            casOvercurrent = new ContinuousAudioDataStream(asOvercurrent.getData());
            casUndervolt = new ContinuousAudioDataStream(asUndervolt.getData());
            casBattOvertemp = new ContinuousAudioDataStream(asBattOvertemp.getData());
            casHSOvertemp = new ContinuousAudioDataStream(asHSOvertemp.getData());
            casMotCtrlErr = new ContinuousAudioDataStream(asMotCtrlErr.getData());
            casPan = new ContinuousAudioDataStream(asPan.getData());
            casMayday = new ContinuousAudioDataStream(asMayday.getData());


        } catch (IOException e) {
            e.printStackTrace();
        }

        playingOvervoltAlarm = false;
        playingOvercurrentAlarm = false;
        playingUndervoltAlarm = false;
        playingBattOvertempAlarm = false;
        playingHSOvertempAlarm = false;
        playingMotCtrlErrAlarm = false;
        playingPanAlarm = false;
        playingMaydayAlarm = false;

        MessageHandler.get().subscribeData("bmsvoltextremes", this);
		MessageHandler.get().subscribeData("bmstempextremes", this);
        MessageHandler.get().subscribeData("ab_current", this);
        MessageHandler.get().subscribeData("motflag", this);
		MessageHandler.get().subscribeData("motflag2", this);
        //MessageHandler.get().subscribeData("squawk", this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
		switch (message.getId()) {
                        case "ab_current":
                            if((message.get("battery") < -48.75 || message.get("battery") > 48.75) && 
                                    !playingOvercurrentAlarm)
                            {
                                if (enableOvercurrent.isSelected()) {
                                    AudioPlayer.player.start(casOvercurrent);
                                    playingOvercurrentAlarm = true;
					}
                                enableOvercurrent.setForeground(Color.RED);
                            }
                            
                            else if((message.get("battery") > -48.75 && message.get("battery") < 48.75) && 
                                    playingOvercurrentAlarm){
                                AudioPlayer.player.stop(casOvercurrent);
                                playingOvercurrentAlarm = false;
                                enableOvercurrent.setForeground(Color.BLACK);
                            }
                            break;
			case "bmsvoltextremes":
				//System.out.println(message.get("vmin") + " " + playingAlarm);
				if (message.get("max") > 4.2 && !playingOvervoltAlarm) {
					if (enableOvervolt.isSelected()) {
						AudioPlayer.player.start(casOvervolt);
						playingOvervoltAlarm = true;
					}
					enableOvervolt.setForeground(Color.RED);
				} else if (message.get("max") <= 4.2 && playingOvervoltAlarm) {
					playingOvervoltAlarm = false;
					AudioPlayer.player.stop(casOvervolt);
					enableOvervolt.setForeground(Color.BLACK);
				}
				if (message.get("min") < 2.5 && !playingUndervoltAlarm) {

					if (enableUndervolt.isSelected()) {
						AudioPlayer.player.start(casUndervolt);
						playingUndervoltAlarm = true;
					}
					enableUndervolt.setForeground(Color.RED);
				} else if (message.get("min") >= 2.5 && playingUndervoltAlarm) {
					playingUndervoltAlarm = false;
					AudioPlayer.player.stop(casUndervolt);
					enableUndervolt.setForeground(Color.BLACK);
				}
				break;
			case "bmstempextremes":
				if (message.get("max") > 57 && !playingBattOvertempAlarm) {
					if (enableBattOvertemp.isSelected()) {
						AudioPlayer.player.start(casBattOvertemp);
						playingBattOvertempAlarm = true;
					}
					enableBattOvertemp.setForeground(Color.RED);

				} else if (message.get("min") <= 57 && playingBattOvertempAlarm) {
					playingBattOvertempAlarm = false;
					AudioPlayer.player.stop(casBattOvertemp);
					enableBattOvertemp.setForeground(Color.BLACK);
				}
				break;
			case "motflag":
				if (message.get("err") != 0 && !playingMotCtrlErrAlarm) {
					playingMotCtrlErrAlarm = true;
					if (enableMotCtrlErr.isSelected()) {
						AudioPlayer.player.start(casMotCtrlErr);
						playingMotCtrlErrAlarm = true;
					}
					enableMotCtrlErr.setForeground(Color.RED);
				} else if (message.get("err") == 0 && playingMotCtrlErrAlarm) {
					playingMotCtrlErrAlarm = false;
					AudioPlayer.player.stop(casMotCtrlErr);
					enableMotCtrlErr.setForeground(Color.BLACK);
				}
				break;
			case "motflag2":
				if (message.get("err") != 0 && !playingMotCtrlErrAlarm) {
					playingMotCtrlErrAlarm = true;
					if (enableMotCtrlErr.isSelected()) {
						AudioPlayer.player.start(casMotCtrlErr);
						playingMotCtrlErrAlarm = true;
					}
					enableMotCtrlErr.setForeground(Color.RED);
				} else if (message.get("err") == 0 && playingMotCtrlErrAlarm) {
					playingMotCtrlErrAlarm = false;
					AudioPlayer.player.stop(casMotCtrlErr);
					enableMotCtrlErr.setForeground(Color.BLACK);
				}
				break;
			/*case "squawk":
				if (message.get("code").intValue() == 5 && !playingPanAlarm) {
					playingPanAlarm = true;
					if (enablePan.isSelected()) {
						AudioPlayer.player.start(casPan);
						playingPanAlarm = true;
					}
					enablePan.setForeground(Color.RED);
				} else if (message.get("code") != 5 && playingPanAlarm) {
					playingPanAlarm = false;
					AudioPlayer.player.stop(casPan);
					enablePan.setForeground(Color.BLACK);
				}
				break;*/
			default:
				break;
		}
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == enableOvercurrent) {
            if (!enableOvercurrent.isSelected() && playingOvercurrentAlarm) {
                playingOvercurrentAlarm = false;
                AudioPlayer.player.stop(casOvercurrent);
            }
        }
        if (source == enableOvervolt) {
            if (!enableOvervolt.isSelected() && playingOvervoltAlarm) {
                playingOvervoltAlarm = false;
                AudioPlayer.player.stop(casOvervolt);
            }
        }
        if (source == enableUndervolt) {
            if (!enableUndervolt.isSelected() && playingUndervoltAlarm) {
                playingUndervoltAlarm = false;
                AudioPlayer.player.stop(casUndervolt);
            }
        }
        if (source == enableBattOvertemp) {
            if (!enableBattOvertemp.isSelected() && playingBattOvertempAlarm) {
                playingBattOvertempAlarm = false;
                AudioPlayer.player.stop(casBattOvertemp);
            }
        }
        if (source == enableHSOvertemp) {
            if (!enableHSOvertemp.isSelected() && playingHSOvertempAlarm) {
                playingHSOvertempAlarm = false;
                AudioPlayer.player.stop(casHSOvertemp);
            }
        }
        if (source == enableMotCtrlErr) {
            if (!enableMotCtrlErr.isSelected() && playingMotCtrlErrAlarm) {
                playingMotCtrlErrAlarm = false;
                AudioPlayer.player.stop(casMotCtrlErr);
            }
        }
    }
}