package network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.print.DocFlavor.STRING;

import resources.Util;

/**
 * Class that represents a peer in the network
 */
public class Subscriber implements Serializable{

	private static final long serialVersionUID = 1L;

	private InetAddress address = null;
	private Integer defPort = -1;			//For Topology/Activity Messages
	private Integer mcPort = -1;			//For Protocol Messages
	private Integer mdrPort = -1;			
	private Integer mdbPort = -1;

	/**
	 * COnstructor. The address may be a String or an InetAddress
	 * @param address
	 * @param defPort
	 * @param <T>
	 */
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

	/**
	 * Constructor. The address may be a String or an Inet Address
	 * @param address
	 * @param defPort
	 * @param mcPort
	 * @param mdrPort
	 * @param mdbPort
	 * @param <T>
	 */
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

	/**
	 * Function that sets the subscriber properties
	 * @param address
	 * @param defPort
	 * @param mcPort
	 * @param mdrPort
	 * @param mdbPort
	 * @param <T>
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

	/**
	 * Function that returns the Suscriber address
	 * @return
	 */
	public InetAddress getAddress(){
		return address;
	}

	/**
	 * Function that returns the DefaultPort (Comunication with the tracker)
	 * @return
	 */
	public int getDefPort(){
		return defPort;
	}

	/**
	 * Function that returns the McPort
	 * @return
	 */
	public int getMcPort(){
		return mcPort;
	}

	/**
	 * Function that returns the MdrPort
	 * @return
	 */
	public int getMdrPort(){
		return mdrPort;
	}

	/**
	 * Function that returns the MdbPort
	 * @return
	 */
	public int getMdbPort(){
		return mdbPort;
	}

	/**
	 * Function that returns the correct port depending on the channel type
	 * @param type
	 * @return
	 */
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

	/**
	 * Function that sets the address of the Subscriber
	 * @param address
	 */
	public void setAddress(InetAddress address){
		this.address = address;
	}

	/**
	 * Functions that sets all the ports of the Subscriber
	 * @param defPort
	 * @param mcPort
	 * @param mdrPort
	 * @param mdbPort
	 */
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
