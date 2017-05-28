package network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.print.DocFlavor.STRING;

import resources.Util;

public class Subscriber implements Serializable{

	private static final long serialVersionUID = 1L;

	private InetAddress address = null;
	private Integer defPort = -1;			//For Topology/Activity Messages
	private Integer mcPort = -1;			//For Protocol Messages
	private Integer mdrPort = -1;			
	private Integer mdbPort = -1;
	
	public <T> Subscriber(T address,int defPort){
		
		try {
			if(address instanceof InetAddress)
				this.address = (InetAddress)address;
			else
				this.address = InetAddress.getByName((String) address);
			this.defPort = defPort;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	public <T> Subscriber(T address,int defPort, int mcPort, int mdrPort, int mdbPort){
        
		try {
			if(address instanceof String)
				this.address = InetAddress.getByName((String)address);
			else
				this.address = (InetAddress) address;
			this.defPort = defPort;
			this.mcPort = mcPort;
			this.mdrPort = mdrPort;
			this.mdbPort = mdbPort;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * SETS AND GETS
	 */
	
	public <T> void setSubscriber(T address, int defPort, int mcPort, int mdrPort, int mdbPort){
		
		try {
			if(address instanceof String)
				this.address = InetAddress.getByName((String)address);
			else
				this.address = (InetAddress)address;
			this.defPort = defPort;
			this.mcPort = mcPort;
			this.mdrPort = mdrPort;
			this.mdbPort = mdbPort;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getAddress(){
		return address;
	}
	
	public int getDefPort(){
		return defPort;
	}
	
	public int getMcPort(){
		return mcPort;
	}
	
	public int getMdrPort(){
		return mdrPort;
	}
	
	public int getMdbPort(){
		return mdbPort;
	}
	
	public int getPort(Util.ChannelType type){
		switch (type) {
		case TOP:
			return defPort;
		case MC:
			return mcPort;
		case MDR:
			return mdrPort;
		case MDB:
			return mdbPort;
		default:
			break;
		}
		return -1;
	}
	
	public void setAddress(InetAddress address){
		this.address = address;
	}
	
	public void setPorts(Integer defPort, Integer mcPort, Integer mdrPort, Integer mdbPort){
		if(defPort != null)		this.defPort = defPort;
		if(mcPort != null)		this.mcPort = mcPort;
		if(mdrPort != null) 	this.mdrPort = mdrPort;
		if(mdbPort != null) 	this.mdbPort = mdbPort;
	}

	@Override
	public String toString(){
		String s = new String("");
		s += "<" + address.getHostAddress() + ":" + defPort;
		
		if(mcPort != -1)
			s+="|" + mcPort;
		if(mdrPort != -1)
			s+= "|" + mdrPort;
		if(mdbPort != -1)
			s+= "|" + mdbPort;
		
		s += ">";
			
		return s;
	}
	
	@Override
	public boolean equals(Object other){
		
		return (this.address.getHostAddress().compareTo(((Subscriber) other).address.getHostAddress()) == 0 && 
				this.defPort == ((Subscriber) other).getDefPort());	
	}
	
	@Override
	public int hashCode() {
	    int hashCode = 1;

	    hashCode = hashCode * 7 + this.address.hashCode();
	    hashCode += hashCode * 7 + this.defPort.hashCode();

	    return hashCode;
	}
}
