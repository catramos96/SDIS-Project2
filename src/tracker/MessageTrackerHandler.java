package tracker;
import message.TopologyMessage;
import network.Subscriber;
import resources.Logs;
import resources.Util;
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
            //I'm new Try to add me
            case GETONLINE:{
                ArrayList<Subscriber> lastOnline = tracker.getLastAccess(msg.getSubscriberN());
                TopologyMessage m;

                for(Subscriber s : lastOnline) {
                    m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,s,"0");
                    tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
                }
                break;
            }
			case GET:{
                ArrayList<Subscriber> subs = tracker.getDTH(msg.getKey(),msg.getSubscriberN(),msg.getPagination());
                TopologyMessage m;

                for(Subscriber s : subs) {
                    m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,s,msg.getKey());
                    tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
                }
				break;
			}
			case PUT:{
                tracker.putDTH(msg.getKey(),msg.getSubscriber());
			}
            default:{
                System.out.println("RECEIVED UNKNOWN MSG");
                break;
            }
	    }
	}
}
