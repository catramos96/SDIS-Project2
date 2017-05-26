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

               int size = lastOnline.size();
               int i = 0,j = 0;

               //because it has a limit of MAX_N_SUBSCRIBERS that can be sent per message
               while(i < size){
                   if((j = i+Util.MAX_N_SUBSCRIBERS) >= size)
                       j = size;

                   m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBERS,new ArrayList<Subscriber>(lastOnline.subList(i,j)));
                   tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
                   Logs.sentTopologyMessage(m);

                   i = j;
               }

                break;
            }
			case PUT: {
				tracker.putDHT(msg.getKey(),msg.getSubscriber());
				break;
			}
			case GET: {
				ArrayList<Subscriber> subs = tracker.getDHT(msg.getKey(),msg.getSubscriberN(),msg.getPagination());
				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBERS,subs);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case CHECK:{
				int repDeg = tracker.checkDHT(msg.getKey());
				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.INFO,msg.getKey(),repDeg);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case REMOVE:{
				int repDeg = tracker.remSubscriberDHT(msg.getKey(),msg.getSubscriber());
				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.INFO,msg.getKey(),repDeg);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case DELETE:{
				tracker.deleteDHT(msg.getKey());
				break;
			}
            default:{
                System.out.println("RECEIVED UNKNOWN MSG");
                break;
            }
	    }
	}
}
