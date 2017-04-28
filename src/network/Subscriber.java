package network;

import java.net.InetAddress;

public class Subscriber {

	private InetAddress address = null;
	private int port = -1;
	
	public Subscriber(InetAddress address,int port){
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress(){
		return address;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setAddress(InetAddress address){
		this.address = address;
	}
	
	public void setPort(int port){
		this.port = port;
	}
}
