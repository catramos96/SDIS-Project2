package security;

import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import tracker.Tracker;

public class SSLlistenerServer implements Runnable {

	private String[] cypherSuites;
	private int port;

	private SSLServerSocket socket;

	private boolean LISTENING = true;
	private Tracker tracker;

	private static final boolean REQUEST_AUTHENTICATION = true;
	/*
	 * For server side
	 */
	public SSLlistenerServer(int port, String [] cypherSuites, Tracker tracker) throws IOException {
		this.port = port;
		this.cypherSuites = cypherSuites;
		this.tracker = tracker;
		createServerSocket();
	}

	private void createServerSocket() throws IOException {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		socket = (SSLServerSocket) ssf.createServerSocket(port);
		socket.setNeedClientAuth(REQUEST_AUTHENTICATION);
		if(cypherSuites.length == 0) {
			socket.setEnabledCipherSuites(ssf.getDefaultCipherSuites());
		} else {
			socket.setEnabledCipherSuites(cypherSuites);
		}


	}
	
	@Override
	public void run() {
	
		System.out.println("SERVER : starting service");
		
		while(LISTENING) {
			try {
				SSLSocket skct = (SSLSocket) socket.accept();
				System.out.println("lauch SSLClientConnection");
				
			    (new Thread(new SSLClientConnection(skct,tracker))).start();		
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
}