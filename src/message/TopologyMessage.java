package message;
import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.TopologyMessageType;

public class TopologyMessage extends Message{
	Subscriber subscriber1 = null;
	Subscriber subscriber2 = null;
	
	/**
	 * WHOISROOT		
	 * ROOT				<address> <port> 	<CRLF><CRLF>
	 * PARENT 			<address> <port> 	<CRLF><CRLF>
	 * SUBSCRIBER 		<address> <port> 	<CRLF><CRLF>
	 * NEWSUBSCRIBER	<address> <port> 	<CRLF><CRLF>
	 * REMSUBSCRIBER    <address> <port>    <CRLF><CRLF>
	 * MOVSUBSCRIBER	<subscriberAddress> <subscriberPort> <parentAddress> <parentPort>	<CRLF><CRLF>
	 */
	
	public TopologyMessage(Util.TopologyMessageType type){
		if(type.name().equals("WHOISROOT")){
			this.type = type;
		}
	}
	
	public TopologyMessage(Util.TopologyMessageType type, Subscriber subscriber)
	{
		if(type.name().equals("ROOT") || type.name().equals("PARENT") || type.name().equals("SUBSCRIBER") || 
				type.name().equals("NEWSUBSCRIBER") ||type.name().equals("REMSUBSCRIBER")){
			this.type = type;
			this.subscriber1 = subscriber;
		}
	}
	
	public TopologyMessage(Util.TopologyMessageType type, Subscriber s1, Subscriber s2)
	{
		if(type.name().equals("MOVSUBSCRIBER")){
			this.type = type;
			this.subscriber1 = s1;
			this.subscriber2 = s2;
		}
	}
	
	@Override
	public byte[] buildMessage(){
		
		String content = type.name() + " ";
		
		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.WHOISROOT) != 0)
			content += subscriber1.getAddress().getHostAddress() + " " + subscriber1.getPort() + " ";
		
		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.MOVSUBSCRIBER) == 0){
			content += subscriber2.getAddress().getHostAddress() + " " + subscriber2.getPort() + " ";
		}
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		
		return content.getBytes();
	}
	
	public static TopologyMessage parseMessage(byte[] message){
		TopologyMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.TopologyMessageType type_rcv;
		
		if((type_rcv = Util.TopologyMessageType.valueOf(parts[0])) != null){
		
			Subscriber s1 = null;
			Subscriber s2 = null;
			
			if(type_rcv.compareTo(Util.TopologyMessageType.WHOISROOT) == 0)
				parsed = new TopologyMessage(type_rcv);
			else if(type_rcv.compareTo(Util.TopologyMessageType.ROOT) == 0 || type_rcv.compareTo(Util.TopologyMessageType.PARENT) == 0 ||
					type_rcv.compareTo(Util.TopologyMessageType.SUBSCRIBER) == 0 || type_rcv.compareTo(Util.TopologyMessageType.NEWSUBSCRIBER) == 0 ||
					type_rcv.compareTo(Util.TopologyMessageType.REMSUBSCRIBER) == 0){
				s1 = new Subscriber(parts[1],Integer.parseInt(parts[2]));
				parsed = new TopologyMessage(type_rcv,s1);	
			}
			else if(type_rcv.compareTo(Util.TopologyMessageType.MOVSUBSCRIBER) == 0){
				s1 = new Subscriber(parts[1],Integer.parseInt(parts[2]));
				s2 = new Subscriber(parts[3],Integer.parseInt(parts[4]));
				parsed = new TopologyMessage(type_rcv,s1,s2) ;
			}
		}
		else{
			Logs.notTopologyMessage();
		}
		
		return parsed;
	}
	
	/*
	 * Gets and Sets
	 */

	public Util.TopologyMessageType getType(){
		return (TopologyMessageType) type;
	}
	
	public Subscriber getSubscriber1(){
		return subscriber1;
	}
	
	public Subscriber getSubscriber2(){
		return subscriber2;
	}
}
