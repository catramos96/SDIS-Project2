package tracker;

import java.nio.channels.Channel;
import java.util.ArrayList;

import message.ActivityMessage;
import message.TopologyMessage;
import network.Subscriber;
import resources.Logs;
import resources.Util;

public class MessageTrackerHandler extends Thread {
	private Tracker tracker = null;
	private Subscriber sender = null;
	
	public MessageTrackerHandler(byte[] message, Subscriber sender, Tracker tracker){
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
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
			Logs.sentTopologyMessage(message);
			break;
		}
		//I'm new Try to add me
		case NEWSUBSCRIBER:{	
			Subscriber parent = null;
			
			//Send root
			if(tracker.getRoot() == null){
				Logs.newTopology("ROOT", sender);
				tracker.setRoot(sender);
			}
			
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.ROOT,tracker.getRoot());
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
			Logs.sentTopologyMessage(message);
			
			if(tracker.hasSubscriber(sender)){
				if(tracker.getRoot().equals(sender))
					break;
				
				parent = tracker.getParent(sender);
				
				message = new TopologyMessage(Util.TopologyMessageType.PARENT,parent);
				tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
				Logs.sentTopologyMessage(message);
				break;
			}
			
			//Add to topology
			parent = tracker.addToTopology(sender);
			Logs.newTopology("SUBSCRIBER", sender);
			
			if(parent == null) break;
			else if(tracker.getRoot().equals(msg.getSubscriber1())){	//parent of the root -> parent is the new root
				tracker.setRoot(parent);
				Logs.newTopology("ROOT", parent);
			}
			else{
				//send parent
				message = new TopologyMessage(Util.TopologyMessageType.PARENT,parent);
				tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
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
		
		Logs.activityMessage(msg, sender);
		
		switch (msg.getType()) {
		case ONLINE:{
			tracker.setSubscriberActivity(sender, true);
			break;
		}
		case OFFLINE:{
			tracker.subscriberOffline(sender);
			break;
		}
		default:{
			System.out.println("ERROR: Wrong type of activity messaeg received");
			break;
		}
		}
		
	}
}
