package solarcar.vdcSim.frontEnd;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import solarcar.vdcSim.Message;

public class VDCConsole extends JFrame implements DocumentListener, ActionListener {

    private final int maxLines = 500;
    private JScrollPane scrollPane;
    private JTextArea textArea;
	private JPanel globalOptionsPanel;
	//private JPanel messagePanel;
	private JScrollPane messageListScroll;
	private JButton loadButton;
	private JButton saveButton;
	private JFileChooser fileChooser;
	
	private static VDCConsole vdcc = null;

	public static VDCConsole get() {
		if(vdcc == null) {
			vdcc = new VDCConsole();
			vdcc.start();
		}
		return vdcc;
	}
	
	private static void restart() {
		vdcc.stop();
		vdcc = null;
		get();
	}
	
    private VDCConsole() {
        super("Vehicle Data Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font("Lucida Console", Font.PLAIN, 12);
        textArea = new JTextArea(20, 80);
        textArea.setEditable(false);
        textArea.setFont(font);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);

        scrollPane = new JScrollPane(textArea);
        // not autoscrolling

		globalOptionsPanel = new JPanel();
		globalOptionsPanel.setPreferredSize(new Dimension(200, 100));
		messageListScroll = new JScrollPane(MessageHandler.get().getMessageList());
		messageListScroll.setPreferredSize(new Dimension(200, 300));
		
		GridBagConstraints c = new GridBagConstraints();
		
		setLayout(new GridBagLayout());	
		
		c.gridx = 0; c.gridy = 0;
		c.gridheight = 1; c.gridwidth = 2;
        getContentPane().add(scrollPane, c);
		
		c.gridx = 0; c.gridy = 1;
		c.gridheight = 1; c.gridwidth = 1;
		getContentPane().add(globalOptionsPanel, c);
		
		c.gridx = 0; c.gridy = 2;
		getContentPane().add(messageListScroll, c);
		
		c.gridx = 1; c.gridy = 1;
		c.gridheight = 2; c.gridwidth = 1;
		getContentPane().add(MessageHandler.get().getMessagePanel(), c);
		
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
	
		globalOptionsPanel.add(loadButton);
		globalOptionsPanel.add(saveButton);
		
		fileChooser = new JFileChooser();
		
		//setSize(new Dimension(1024, 700));
        pack();
    }

    public void start() {
        setVisible(true);
		textArea.getDocument().addDocumentListener(this);
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
        changeOutputStream();
    }
	
	public void stop() {
		this.dispose();
	}

    private void updateTextArea(final String input) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                textArea.append(input);
            }
        });
    }

    private void changeOutputStream() {
        OutputStream ostream = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf(b));
            }

            @Override
            public void write(byte b[]) throws IOException {
                updateTextArea(new String(b));
            }

            @Override
            public void write(byte b[], int off, int len) {
                updateTextArea(new String(b, off, len));
            }
        };
        System.setOut(new PrintStream(ostream));
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                removeLines(e);
            }
        });
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void removeLines(DocumentEvent e) {

        Document document = e.getDocument();
        Element root = document.getDefaultRootElement();

        while (root.getElementCount() > maxLines) {
            Element line = root.getElement(0);
            int end = line.getEndOffset();

            try {
                document.remove(0, end);
                textArea.setCaretPosition(e.getLength());
            } catch (BadLocationException ble) {
                System.out.println(ble);
            }
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == saveButton) {
			int retval = fileChooser.showSaveDialog(this);
			
			if(retval == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
					out.writeObject(MessageHandler.get().getMessageList());
					out.writeObject(MessageHandler.get().getMessagePanel());
					out.flush();
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
					Logger.getLogger(VDCConsole.class.getName()).log(Level.SEVERE, null, ex);
                                        System.out.println("Couldn't save profile");
				}
			}
			
		} else if (e.getSource() == loadButton) {
			int retval = fileChooser.showOpenDialog(this);
			
			if(retval == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
					JList<Message> msglist = (JList<Message>) in.readObject();
					JPanel msgpanel = (JPanel) in.readObject();
					MessageHandler.replace(msglist, msgpanel);
					restart();
					in.close();
				} catch ( IOException | ClassNotFoundException ex) {
					ex.printStackTrace();
					Logger.getLogger(VDCConsole.class.getName()).log(Level.SEVERE, null, ex);
                                        System.out.println("Couldn't load profile");
				}
			}
		}
	}
}
