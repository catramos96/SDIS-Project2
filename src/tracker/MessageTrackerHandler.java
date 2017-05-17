package tracker;
import message.ActivityMessage;
import message.TopologyMessage;
import network.Subscriber;
import resources.Logs;
import resources.Util;

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
		else if(Util.isActivityMessageType(type)){
			ActivityMessage msg = ActivityMessage.parseMessage(message);
			handleActivityMessage(msg);
		}
		else{
			System.out.println(content);
		}
	}
	
	public void handleTopologyMessage(TopologyMessage msg){
		
		Logs.receivedTopologyMessage(msg);
		
		switch (msg.getType()) {
		//Who is the root ?
		case WHOISROOT:{			
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.ROOT,tracker.getRoot());
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getDefPort());
			Logs.sentTopologyMessage(message);
			break;
		}
		//I'm new Try to add me
		case NEWSUBSCRIBER:{	
			Subscriber parent = null;
			
			//Send root
			if(tracker.getRoot() == null){
				tracker.setRoot(msg.getSubscriber());
				Logs.newTopology("ROOT", tracker.getRoot());
			}
			
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.ROOT,tracker.getRoot());
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getDefPort());
			Logs.sentTopologyMessage(message);
			
			if(tracker.hasSubscriber(msg.getSubscriber())){
				if(tracker.getRoot().equals(msg.getSubscriber()))
					break;
				
				parent = tracker.getInfo(msg.getSubscriber()).parent;
				
				if(parent != null){
					message = new TopologyMessage(Util.TopologyMessageType.PARENT,parent);
					tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getDefPort());
					Logs.sentTopologyMessage(message);
				}
				break;
			}
			
			//Add to topology
			parent = tracker.addToTopology(msg.getSubscriber());
			Logs.newTopology("SUBSCRIBER", msg.getSubscriber());
			tracker.setSubscriberActivity(msg.getSubscriber(), true);
			
			if(parent == null) break;
			else if(tracker.getRoot().equals(msg.getSubscriber())){	//parent of the root -> parent is the new root
				tracker.setRoot(parent);
				Logs.newTopology("ROOT", parent);
			}
			else{
				//send parent
				message = new TopologyMessage(Util.TopologyMessageType.PARENT,parent);
				tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getDefPort());
				Logs.sentTopologyMessage(message);
			}
			break;
		}
		default:{
			System.out.println("RECEIVED UNKNOWN MSG");
			break;
		}
	 }
	}
	
	public void handleActivityMessage(ActivityMessage msg){
		
		Logs.activityMessage(msg,msg.getSubscriber());
		
		switch (msg.getType()) {
		case ONLINE:{
			tracker.setSubscriberActivity(msg.getSubscriber(), true);
			break;
		}
		case OFFLINE:{
			tracker.subscriberOffline(msg.getSubscriber());
			break;
		}
		default:{
			System.out.println("ERROR: Wrong type of activity messaeg received");
			break;
		}
		}
		
	}
}
