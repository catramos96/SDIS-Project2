package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Subscriber {

	private InetAddress address = null;
	private int port = -1;
	
	public Subscriber(InetAddress address,int port){
		this.address = address;
		this.port = port;
	}
	
	public Subscriber(String address,int port){
		try {
			this.address = InetAddress.getByName(address);
			this.port = port;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * SETS AND GETS
	 */
	
	public void setSubscriber(InetAddress address, int port){
		this.address = address;
		this.port = port;
	}
	
	public void setSubscriber(String address, int port){
		try {
			this.address = InetAddress.getByName(address);
			this.port = port;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public boolean equals(Object other){
		return (this.address == ((Subscriber)other).getAddress() && 
				this.port == ((Subscriber) other).getPort());
		
	}
}
