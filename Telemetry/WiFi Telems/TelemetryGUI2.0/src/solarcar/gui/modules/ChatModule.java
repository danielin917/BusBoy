package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.gui.guiElements.FlashingTabbedPane;
import solarcar.vdcListener.ChatMessageSubscriber;
import solarcar.vdcListener.SolarChatMessage;
import solarcar.vdcListener.VDCConn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;

public class  ChatModule extends SolarPanel implements ActionListener, DataMessageSubscriber, ChatMessageSubscriber {
    //JTextField chat;

    JTextArea chat;
    JTextField input;
    JTextField name;
    JScrollPane chatScroll;
    JPanel bottomPanel;
    JButton sendButton, sendToBlueButton, sendToDashButton;

    public ChatModule() {
        super();
        setBorder(new TitledBorder("Chat"));
        //setBorder(null);
        setPreferredSize(new Dimension(TelemetryGUI.HalfGraph.width * 2, TelemetryGUI.HalfGraph.height));
        setLayout(new BorderLayout());

        chat = new JTextArea();
        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);

        chatScroll = new JScrollPane(chat);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        input = new JTextField(50);

        name = new JTextField(4);
        name.setText(System.getProperty("user.name"));

        sendButton = new JButton("Send");
        sendButton.setMnemonic(KeyEvent.VK_S);
        //sendButton.setActionCommand("send");

        sendToBlueButton = new JButton("Send to Blue");
        sendToBlueButton.setMnemonic(KeyEvent.VK_B);
        sendToBlueButton.setActionCommand("send-blue");
        

        sendToDashButton = new JButton("Send to Dash");
        sendToDashButton.setMnemonic(KeyEvent.VK_D);
        sendToDashButton.setActionCommand("send-dash");
        

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        bottomPanel.add(name);
        bottomPanel.add(input);
        bottomPanel.add(sendButton);
        bottomPanel.add(sendToBlueButton);
        bottomPanel.add(sendToDashButton);

        add(chatScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void init() {
        MessageHandler.get().subscribeChat(this);
        MessageHandler.get().subscribeData("driverresponse", this);
		input.addActionListener(this);
        sendButton.addActionListener(this);
		sendToBlueButton.addActionListener(this);
		sendToDashButton.addActionListener(this);
    }
    private ConcurrentHashMap<String, String> map;
    private String id;
    private String message;
    private String type;
    private final char squoteMarker = '`';

    @Override
    public void parseChatMessage(SolarChatMessage msg) {
        System.out.println("Chat from " + msg.getId() + ": " + msg.getText().replace(squoteMarker, '\''));
        if (!isShowing()) {
            FlashingTabbedPane.get().flash(8, Color.YELLOW, Color.BLUE);
        }
        chat.append(msg.getId() + " (" + Calendar.getInstance().getTime() + "): " + msg.getText().replace(squoteMarker, '\'') + "\n");
        chat.setCaretPosition(chat.getDocument().getLength());
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        if (msg.getId().equals("driverresponse")) {
            if (!isShowing()) {
                FlashingTabbedPane.get().flash(8, Color.YELLOW, Color.BLUE);
            }

            if ((int) (msg.get("status").intValue()) == 1) {
                chat.append("Blue (" + Calendar.getInstance().getTime() + "): YES!!\n");
            } else {
                chat.append("Blue (" + Calendar.getInstance().getTime() + "): NO!!\n");
            }
            chat.setCaretPosition(chat.getDocument().getLength());
        }
    }
	
    @Override
    public void actionPerformed(ActionEvent e) {
        String sName = name.getText();
        String msg = input.getText().replace('\'', squoteMarker);
        switch (e.getActionCommand()) {
            case "send-blue":
                sName = sName + " -> Blue";
                VDCConn.get().sendMessage("chat-blue msg='" + msg + " ' id='" + sName + "'");
                break;

            case "send-dash":
                sName = sName + " -> Blue (Dash)";
                while (msg.length() < 8) {
                    msg = msg + " ";
                }
                System.out.println("message is " + msg);
                msg = msg.substring(0, 8);
                System.out.println("message is " + msg);
                String sendText = "";
                for (int x = 0; x < 8; x++) {
                    sendText += Integer.toHexString(x < msg.length() ? msg.charAt(x) : 'A');
                }
                VDCConn.get().sendMessage("can addr='0x2C0' data='0x" + sendText + "' len='8' priority='0' ext='0' rem='0'");
                break;

            default:
                break;
        }

        VDCConn.get().sendMessage("chat msg='" + msg + " ' id='" + sName + "'");

        input.setText("");
    }
}