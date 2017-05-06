package message;

import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.ActivityMessageType;

public class ActivityMessage extends Message{
	
	/**
	 * ACTIVITY <CRLF><CRLF>
	 * ONLINE	<CRLF><CRLF>
	 * OFFLINE	<CRLF><CRLF>
	 */
	
	public ActivityMessage(ActivityMessageType type){
		if(type.name().equals("ACTIVITY") || type.name().equals("ONLINE") || type.name().equals("OFFLINE")){
			this.type = type;
		}
	}
	
	@Override
	public byte[] buildMessage(){
		String content = type.name() + " " + Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		return content.getBytes();
	}
	
	public static ActivityMessage parseMessage(byte[] message){
		ActivityMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.ActivityMessageType type_rcv;
		
		if((type_rcv = Util.ActivityMessageType.valueOf(parts[0])) != null)
			parsed = new ActivityMessage(type_rcv);	
		else
			Logs.notActivityMessage();
		
		return parsed;
	}

	public Util.ActivityMessageType getType(){
		return (ActivityMessageType) type;
	}
}
