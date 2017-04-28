package message;

import java.net.InetAddress;
import resources.Util;

public class TopologyMessage {
	
	Util.TopologyMessageType type = null;
	String subscriberAddress = null;
	String parentAddress = null;
	int subscriberPort = -1;
	int parentPort = -1;
	
	/*
	 * ROOT, PARENT, SUBSCRIBER, MOVSUBSCRIBER
	 */
	
	/**
	 * ROOT				<address> <port> 	<CRLF><CRLF>
	 * PARENT 			<address> <port> 	<CRLF><CRLF>
	 * SUBSCRIBER 		<address> <port> 	<CRLF><CRLF>
	 * NEWSUBSCRIBER	<address> <port> 	<CRLF><CRLF>
	 * REMSUBSCRIBER    <address> <port>    <CRLF><CRLF>
	 * MOVSUBSCRIBER	<subscriberAddress> <subscriberPort> <parentAddress> <parentPort>	<CRLF><CRLF>
	 */
	
	public TopologyMessage(Util.TopologyMessageType type, String address, int port)
	{
		if(type.name().equals("ROOT") || type.name().equals("PARENT") || type.name().equals("SUBSCRIBER") || 
				type.name().equals("NEWSUBSCRIBER") ||type.name().equals("REMSUBSCRIBER")){
			this.type = type;
			this.subscriberAddress = address;
			this.subscriberPort = port;
		}
	}
	
	public TopologyMessage(Util.TopologyMessageType type, String address1, int port1, String address2, int port2)
	{
		if(type.name().equals("MOVSUBSCRIBER")){
			this.type = type;
			this.subscriberAddress = address1;
			this.subscriberPort = port1;
			this.parentAddress = address2;
			this.parentPort = port2;
		}
	}
	
	public byte[] buildMessage(){
		
		String content = type.name() + " " + subscriberAddress + " " + subscriberPort + " ";
		
		if(type.compareTo(Util.TopologyMessageType.MOVSUBSCRIBER) == 0){
			content += parentAddress + " " + parentPort + " ";
		}
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		return content.getBytes();
	}
	
	public static TopologyMessage parseMessage(String[] parts){
		TopologyMessage parsed = null;
		
		Util.TopologyMessageType type_rcv;
		
		if((type_rcv = Util.TopologyMessageType.valueOf(parts[0])) != null){
		
			String addr1_rcv = parts[1];
			int port1_rcv = Integer.parseInt(parts[2]);
			
			String addr2_rcv = "";
			int port2_rcv = -1;
			
			if(type_rcv.compareTo(Util.TopologyMessageType.MOVSUBSCRIBER) == 0){
				addr2_rcv = parts[3];
				port2_rcv = Integer.parseInt(parts[4]);
			}
			
			if(type_rcv.compareTo(Util.TopologyMessageType.ROOT) == 0 || type_rcv.compareTo(Util.TopologyMessageType.PARENT) == 0 ||
					type_rcv.compareTo(Util.TopologyMessageType.SUBSCRIBER) == 0 || type_rcv.compareTo(Util.TopologyMessageType.NEWSUBSCRIBER) == 0 ||
					type_rcv.compareTo(Util.TopologyMessageType.REMSUBSCRIBER) == 0)
				parsed = new TopologyMessage(type_rcv,addr1_rcv,port1_rcv);	
			else if(type_rcv.compareTo(Util.TopologyMessageType.MOVSUBSCRIBER) == 0)
				parsed = new TopologyMessage(type_rcv,addr1_rcv,port1_rcv,addr2_rcv,port2_rcv) ;
		}
		else{
			//error
		}
		
		return parsed;
	}
	
	/*
	 * Gets and Sets
	 */

	public Util.TopologyMessageType getType(){
		return type;
	}
	
	public String getSubscriberAddress(){
		return subscriberAddress;
	}
	
	public String getParentAddress(){
		return parentAddress;
	}
	
	public int getSubscriberPort(){
		return subscriberPort;
	}
	
	public int getParentPort(){
		return parentPort;
	}
}
