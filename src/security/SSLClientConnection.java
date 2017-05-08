package security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

public class SSLClientConnection implements Runnable{
	
	private PrintWriter out = null;
	private BufferedReader in = null;	
	private Boolean ONLINE = false;
	
	public SSLClientConnection(PrintWriter out2, BufferedReader in2) {
		out = out2;
		in  = in2;
		
		System.out.println("constructor sslConnection");
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
		ONLINE = true;
		
		while(ONLINE) {
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
		
		
		ONLINE = false;
		out.close();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
