package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLlistenerClient extends Thread{

	private InetAddress address;
	private int port;
	private String[] cypherSuites;

	private PrintWriter out;
	private BufferedReader in;

	public SSLlistenerClient(String hostname, int port, String[] cypherSuites) throws UnknownHostException {
		this.address = InetAddress.getByName(hostname);
		this.port = port;
		this.cypherSuites = cypherSuites;
	}


	public void start() {
		System.out.println("Client : starting SSL conection to server");
		SSLSocket socket;
		try {
			socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port);

			if(cypherSuites.length == 0) {
				SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				socket.setEnabledCipherSuites(ssf.getDefaultCipherSuites());
			} else {
				socket.setEnabledCipherSuites(cypherSuites);
			}

			out = new PrintWriter(socket.getOutputStream(),true);
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));


		} catch (IOException e) {
			System.out.println("CLIENT : unnable to star SSL socket");
			e.printStackTrace();
			return;
		}
		String message = "test";

		out.write(message);
		String s;
		try {
			s = in.readLine();
		} catch (IOException e) {
			System.out.println("CLIENT : unable to read from socket");
			e.printStackTrace();
		}

		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
