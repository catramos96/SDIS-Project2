package tracker;

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
		
		switch (msg.getType()) {
		//Who is the root ?
		case WHOISROOT:{
			Logs.receivedWHOISROOTmsg();
			
			if(tracker.getRoot() == null)
				tracker.setRoot(sender);
			
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.ROOT,tracker.getRoot());
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
			break;
		}
		//I'm new Try to add me
		case NEWSUBSCRIBER:{
			Logs.receivedNEWSUBSCRIBERmsg(msg.getSubscriber1());

			Subscriber parent = tracker.addToTopology(sender);
			
			if(parent == null){
				System.out.println("No free peers available");
				break;
			}
			else if(tracker.getRoot().equals(msg.getSubscriber1())){
				tracker.setRoot(parent);
				Logs.newRoot(parent);
			}
			
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.PARENT,parent);
			tracker.getChannel().send(message.buildMessage(), sender.getAddress(), sender.getPort());
			break;
		}
		default:{
			System.out.println("RECEIVED UNKNOWN MSG");
			break;
		}
	 }
	}
	
	public void handleActivityMessage(ActivityMessage msg){
		
		switch (msg.getType()) {
		case ONLINE:{
			tracker.setSubscriberActivity(sender, true);
			break;
		}
		case OFFLINE:{
			ArrayList<Subscriber> nextSubscribers = tracker.getNextSubscribers(sender);
			
			//New Parents
			for(Subscriber s : nextSubscribers){
				Subscriber newParent = tracker.addToTopology(s);
				TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.PARENT,newParent);
				tracker.getChannel().send(message.buildMessage(), s.getAddress(), s.getPort());
			}
			
			tracker.removeActivitySubscriber(sender);
			break;
		}
		default:{
			System.out.println("ERROR: Wrong type of activity messaeg received");
			break;
		}
		}
		
	}
}
