package solarcar.vdc;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class VDCConsole extends JFrame implements DocumentListener {

    private final int maxLines = 500;
    private JScrollPane scrollPane;
    private JTextArea textArea;

    public VDCConsole(String windowName) {
        super(windowName);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font("Lucida Console", Font.PLAIN, 12);
        textArea = new JTextArea(20, 80);
        textArea.setEditable(false);
        textArea.setFont(font);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);

        scrollPane = new JScrollPane(textArea);
        // not autoscrolling

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        pack();
    }

    public void start() {
        setVisible(true);
		textArea.getDocument().addDocumentListener(this);
        changeOutputStream();
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
}
