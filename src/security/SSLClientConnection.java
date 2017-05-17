package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import javax.net.ssl.SSLSocket;

import tracker.Tracker;


public class SSLClientConnection implements Runnable{
	
	private PrintWriter out;
	private BufferedReader in;	
	private Tracker tracker;
	private InetAddress remoteAdress;
	private SSLSocket sckt;
	
	public SSLClientConnection(SSLSocket sckt, Tracker tracker) {
		
		try {
			out = new PrintWriter(sckt.getOutputStream(),true);
			in  = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
			this.tracker = tracker;
			
			this.sckt = sckt;
			remoteAdress = sckt.getInetAddress();
			System.out.println(remoteAdress.getHostAddress());
			System.out.println("constructor sslConnection");
		} catch (IOException e) {
			System.out.println("Unable to start conection");
			e.printStackTrace();
		}
		
		
		
	}
	

	
	void sendMessage(String msg) {
		out.println(msg);
	}
	
	String receiveMessage() throws IOException {
		return in.readLine();
	}

	@Override
	public void run() {
		System.out.println("start sslConnection");
		System.out.println("try to read");
			try {
				String buffer = receiveMessage(); //TODO DEFENIR 1 TImEOUT
				messageHandler(buffer);
				System.out.println("message:" + buffer);
				buffer = "SERVER CLOSING CONNECTION \n";
				sendMessage(buffer); //TEST
				System.out.println(buffer);
				out.close();
				in.close();
				sckt.close();
			} catch (IOException e) {
				System.out.println("SERVER : Fail connection with CLIENT");
				e.printStackTrace();
			}	
	}
	
	
	private void messageHandler(String message) {
		String [] content = message.split(" ");
		
		if(content.length < 1){
			System.out.println("ERRO:" + content);
		}
		else if(content[0].equals("REGIST")) {
			registHandler(content);
		}
		else {
			System.out.println("Dont recognise message:" + content);
		}
	}
	
	private void registHandler(String [] content) {
		tracker.addIP(remoteAdress);
	}
	
}
