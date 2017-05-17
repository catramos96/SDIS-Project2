package message;
import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.TopologyMessageType;

public class TopologyMessage extends Message{
	Subscriber subscriber = null;
	
	/**
	 * WHOISROOT		
	 * ROOT				<address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 * PARENT 			<address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 * SUBSCRIBER 		<address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 * NEWSUBSCRIBER	<address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 * REMSUBSCRIBER    <address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
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
			this.subscriber = subscriber;
		}
	}
	
	@Override
	public byte[] buildMessage(){
		
		String content = type.name() + " ";
		
		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.WHOISROOT) != 0)
			content += subscriber.getAddress().getHostAddress() + " " + 
						subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
						subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		
		return content.getBytes();
	}
	
	public static TopologyMessage parseMessage(byte[] message){
		TopologyMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.TopologyMessageType type_rcv;
		
		if((type_rcv = Util.TopologyMessageType.valueOf(parts[0])) != null){
		
			Subscriber s1 = null;
			int p1,p2,p3,p4;
			
			if(type_rcv.compareTo(Util.TopologyMessageType.WHOISROOT) == 0)
				parsed = new TopologyMessage(type_rcv);
			else{
				p1 = Integer.parseInt(parts[2]);
				p2 = Integer.parseInt(parts[3]);
				p3 = Integer.parseInt(parts[4]);
				p4 = Integer.parseInt(parts[5]);
				s1 = new Subscriber(parts[1],p1,p2,p3,p4);
				parsed = new TopologyMessage(type_rcv,s1);	
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
	
	public Subscriber getSubscriber(){
		return subscriber;
	}
}