/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.gui.guiElements;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author aaresh
 */
class SplashScreenOutputStream extends OutputStream {

	public SplashScreenOutputStream() {
		super();
		buffer = "";
	}

	private String buffer;
	
	/*@Override
	public void write(int b) throws IOException {
		if(String.valueOf(b).equals("\n")) {
			System.out.println("Writing buffer: " + buffer);
			SplashScreenDrawer.get().splashText(buffer);
			buffer = "";
			return;
		}
		buffer += String.valueOf(b);	
		System.out.println("Updated buffer: " + buffer);
	}*/
	
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
	
	private void updateTextArea(final String input) {
		buffer += input;
		//System.out.println("Updated buffer: "+ buffer);
		while(buffer.contains("\n")) {
			SplashScreenDrawer.get().splashText(buffer.substring(0, buffer.indexOf("\n")));
		//	System.out.println("Writing: " + buffer.substring(0, buffer.indexOf("\n")));
			buffer = buffer.substring(buffer.indexOf("\n") + 1);
		}
	}
	
}
