package message;

import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.ActivityMessageType;

public class ActivityMessage extends Message{

	Subscriber parent = null;
	
	/**
	 * ACTIVITY
	 * ONLINE <address> <port> 		<CRLF><CRLF>
	 * OFFLINE <address> <port> 	<CRLF><CRLF>
	 */
	
	public ActivityMessage(ActivityMessageType type){
		if(type.name().equals("ACTIVITY")){
			this.type = type;
		}
	}
	
	public ActivityMessage(Util.ActivityMessageType type, Subscriber subscriber)
	{
		if(type.name().equals("ONLINE") || type.name().equals("OFFLINE")){
			this.type = type;
			this.parent = subscriber;
		}
	}
	
	@Override
	public byte[] buildMessage(){
		
		String content = type.name() + " ";
		
		if(((ActivityMessageType)type).compareTo(Util.ActivityMessageType.ACTIVITY) != 0)
			content += parent.getAddress().getHostAddress() + " " + parent.getPort() + " ";
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		
		return content.getBytes();
	}
	
	public static ActivityMessage parseMessage(byte[] message){
		ActivityMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.ActivityMessageType type_rcv;
		
		if((type_rcv = Util.ActivityMessageType.valueOf(parts[0])) != null){
		
			Subscriber s1 = null;
			
			if(type_rcv.compareTo(Util.ActivityMessageType.ACTIVITY) == 0)
				parsed = new ActivityMessage(type_rcv);
			else if(type_rcv.compareTo(Util.ActivityMessageType.ONLINE) == 0 || type_rcv.compareTo(Util.ActivityMessageType.OFFLINE) == 0){
				s1 = new Subscriber(parts[1],Integer.parseInt(parts[2]));
				parsed = new ActivityMessage(type_rcv,s1);	
			}
		}
		else{
			Logs.notActivityMessage();
		}
		
		return parsed;
	}
	
	/*
	 * Gets and Sets
	 */

	public Util.ActivityMessageType getType(){
		return (ActivityMessageType) type;
	}
	
	public Subscriber getParent(){
		return parent;
	}
}
