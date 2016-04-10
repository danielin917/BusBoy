/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.gui.guiElements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aaresh
 */
public class SplashScreenDrawer extends PrintStream {
	
	private volatile static SplashScreenDrawer ssd = null;
	
	SplashScreen screen;
	Graphics2D g2d;
	private Rectangle2D textArea;
	private Rectangle2D rightTextBlank;
	
	// start text at x,y = 20,230
	
	private SplashScreenDrawer()
	{
		super(new SplashScreenOutputStream());
		
		try {
			getScreen();
			//text = new String[5];
			text = new LinkedBlockingDeque<>(textLen);
			for(int i = 0; i < textLen; i++)
				text.put("");
		} catch (InterruptedException ex) {
			Logger.getLogger(SplashScreenDrawer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void getScreen()
	{
		screen = SplashScreen.getSplashScreen();
		if(screen != null)
		{
			g2d = screen.createGraphics();
			g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
			
			textArea = new Rectangle2D.Double(20, 250, 820, 100);
			rightTextBlank = new Rectangle2D.Double(820, 250, 20, 100);
		}
	}
	public static SplashScreenDrawer get()
	{
		if(ssd == null)
		{
			ssd = new SplashScreenDrawer();
		}
		return ssd;
	}
	
	private LinkedBlockingDeque<String> text;
	private final int textLen = 6;
	
	public void splashText(String str)
	{
		if(screen == null)
		{
			getScreen();
		}
		
		if(screen != null && screen.isVisible())
		{
			g2d.setPaint(new Color(0x2a57a5));
			g2d.fill(textArea);
			
			g2d.setPaint(Color.WHITE);
			
			text.pollLast();
			try {
				text.putFirst(str);
			} catch (InterruptedException ex) {
				Logger.getLogger(SplashScreenDrawer.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			int y = (int) textArea.getY() + 8;
			for(String s : text)
			{
				g2d.drawString(s, (int)textArea.getX(), y);
				y += 10;
			}
			
			g2d.setPaint(new Color(0x2a57a5));
			g2d.fill(rightTextBlank);
			
			screen.update();
		}
	}
	
	
}
