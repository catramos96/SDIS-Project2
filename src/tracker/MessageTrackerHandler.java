package tracker;
import message.TopologyMessage;
import network.Subscriber;
import resources.Logs;
import resources.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MessageTrackerHandler extends Thread {
	private Tracker tracker = null;
	private Subscriber sender = null;
	
	public MessageTrackerHandler(byte[] message, Subscriber sender, Tracker tracker){
		
		if(!tracker.authorizedIP(sender.getAddress().getHostAddress())) {
			return;
		}
		this.tracker = tracker;
		this.sender = sender;
		
		String content = new String(message);

		int firstSpace = content.indexOf(new String(" "));
		String type = content.substring(0,firstSpace);
	
		
		if(Util.isTopologyMessageType(type)){
			TopologyMessage msg = TopologyMessage.parseMessage(message);
			handleTopologyMessage(msg);
		}
		else{
			System.out.println(content);
		}
	}
	
	public void handleTopologyMessage(TopologyMessage msg){
		
		Logs.receivedTopologyMessage(msg);
		
		switch (msg.getType()) {

		    //Update last access of subscriber
            case ONLINE:{
                tracker.registerSubscriber(msg.getSubscriber());
                break;
            }
            //I need the last n subscribers that were online
            case GETONLINE:{
                ArrayList<Subscriber> lastOnline = tracker.getLastAccess(msg.getSubscriberN());
                TopologyMessage m;

               /*
               	MUDAR
               	SÓ PODE MANDAR 10 POR MENSAGEM
				}*/

				m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBERS,lastOnline);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);

                break;
            }
            default:{
                System.out.println("RECEIVED UNKNOWN MSG");
                break;
            }
	    }
	}
}
