package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import peer.Peer;

public class SSLlistenerClient {

	private InetAddress address;
	private int port;
	private String[] cypherSuites;
	private PrintWriter out;
	private BufferedReader in;
	private Peer peer;
	
	public SSLlistenerClient(String hostname, int port, String[] cypherSuites, Peer peer) throws UnknownHostException {
		this.address = InetAddress.getByName(hostname);
		this.port = port;
		this.cypherSuites = cypherSuites;
		this.peer = peer;
	}


	public void start() throws InvalidKeyException, NoSuchAlgorithmException,IOException {
		System.out.println("Client : starting SSL conection to server");
		
		SSLSocket socket;
		
			socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port);

			if(cypherSuites.length == 0) {
				SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				socket.setEnabledCipherSuites(ssf.getDefaultCipherSuites());
			} else {
				socket.setEnabledCipherSuites(cypherSuites);
			}
			
			
			
			
			System.out.println("Client : criar buffers");
			out = new PrintWriter(socket.getOutputStream(),true);
			in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			register();
			
			out.close();
			in.close();
			socket.close();
			
	}
	
	void sendMessage(String msg){
		out.println(msg);
	}

	String receiveMessage() throws IOException {
		return in.readLine();
	}
	
	
	private void register(){
		String message = "REGIST\n";
		System.out.println("Client : antes de enviar");
		out.println(message);
		System.out.println("Eviei mensagem");
	}
}
