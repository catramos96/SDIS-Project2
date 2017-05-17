package message;

import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.ActivityMessageType;

public class ActivityMessage extends Message{
	
	Subscriber subscriber = null;
	
	/**
	 * ACTIVITY <CRLF><CRLF>
	 * ONLINE	<address> <defPort> <mcPort> <mdrPort> <mdbPort> <CRLF><CRLF>
	 * OFFLINE	<address> <defPort> <mcPort> <mdrPort> <mdbPort> <CRLF><CRLF>
	 */
	
	public ActivityMessage(ActivityMessageType type){
		if(type.name().equals("ACTIVITY")){
			this.type = type;
		}
	}
	
	public ActivityMessage(ActivityMessageType type, Subscriber subscriber){
		if(type.name().equals("ONLINE") || type.name().equals("OFFLINE")){
			this.type = type;
			this.subscriber = subscriber;
		}
	}
	
	@Override
	public byte[] buildMessage(){
		String content = type.name() + " ";
		
		if(!type.name().equals("ACTIVITY"))
			content += subscriber.getAddress().getHostAddress() + " " + 
					subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
					subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		
		return content.getBytes();
	}
	
	public static ActivityMessage parseMessage(byte[] message){
		ActivityMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.ActivityMessageType type_rcv;
		
		if((type_rcv = Util.ActivityMessageType.valueOf(parts[0])) != null){
			
			if(type_rcv.compareTo(Util.ActivityMessageType.ACTIVITY) == 0)
				parsed = new ActivityMessage(type_rcv);	
			else{
				int p1,p2,p3,p4;
				
				p1 = Integer.parseInt(parts[2]);
				p2 = Integer.parseInt(parts[3]);
				p3 = Integer.parseInt(parts[4]);
				p4 = Integer.parseInt(parts[5]);
				
				Subscriber s = new Subscriber(parts[1],p1,p2,p3,p4);
				parsed = new ActivityMessage(type_rcv,s);
			}
		}
		else
			Logs.notActivityMessage();
		
		return parsed;
	}

	public Util.ActivityMessageType getType(){
		return (ActivityMessageType) type;
	}
	
	public Subscriber getSubscriber(){
		return subscriber;
	}
}
