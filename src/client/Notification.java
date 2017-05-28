package client;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.net.MalformedURLException;

public class Notification implements Runnable  {
	private String message = null;
	private String protocol = null;
	
    public Notification(String protocol,String msg) {
    	this.message = msg;
    	this.protocol = protocol;
    	
    }

    public void displayTray() throws AWTException, java.net.MalformedURLException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resizes the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage( protocol , message, MessageType.INFO);
    }

	@Override
	public void run() {
		 if (SystemTray.isSupported()) {
	        	try {
					displayTray();
				} catch (MalformedURLException | AWTException e1) {
					System.out.println("Malformed notification");
				}
	            try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("thread sleep failure");
				}
	            return;
	        } else {
	            System.err.println("System tray not supported!");
	        }
		
	}
}