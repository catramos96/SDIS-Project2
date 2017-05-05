package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;

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
		socket.setNeedClientAuth(true);
		if(cypherSuites.length == 0) {
			socket.setEnabledCipherSuites(ssf.getDefaultCipherSuites());
		} else {
			socket.setEnabledCipherSuites(cypherSuites);
		}


	}


	public void start() {
		System.out.println("SERVER : starting service");
		

		while(LISTENING) {
			
			SSLSocket skct;
			PrintWriter out;
			BufferedReader in;

			
			try {
				skct = (SSLSocket) socket.accept();
				out = new PrintWriter(skct.getOutputStream(),true);
				in  = new BufferedReader(new InputStreamReader(skct.getInputStream()));
			} catch (IOException e) {
				System.out.println("SERVER : start service SSL failed");
				LISTENING = false;
				return;
			}
			
			System.out.println("try to read");
			try {
				String buffer = in.readLine(); 
					out.println(buffer); //TEST
				System.out.println(buffer);
			} catch (IOException e) {
				System.out.println("SERVER : Fail to read from socket");
				e.printStackTrace();
			}
			
			
			
		}
	}
}