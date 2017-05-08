package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class SSLlistenerServer extends Thread {

	private String[] cypherSuites;
	private int port;

	private SSLServerSocket socket;

	private boolean LISTENING = true;

	private static final boolean REQUEST_AUTHENTICATION = true;
	/*
	 * For server side
	 */
	public SSLlistenerServer(int port, String [] cypherSuites) throws IOException {
		this.port = port;
		this.cypherSuites = cypherSuites;

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


	public void start() {
		System.out.println("SERVER : starting service");

		
		while(LISTENING) {
			try {
				
				SSLSocket skct = (SSLSocket) socket.accept();
				System.out.println("lauch SSLClientConnection");
				
				PrintWriter	out = new PrintWriter(skct.getOutputStream(),true);
				BufferedReader in  = new BufferedReader(new InputStreamReader(skct.getInputStream()));
				
			    (new Thread(new SSLClientConnection(out,in))).start();
			
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}