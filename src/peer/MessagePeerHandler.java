package peer;
import message.ActivityMessage;
import message.TopologyMessage;
import network.GroupChannel;
import network.Subscriber;
import resources.Logs;
import resources.Util;
public class MessagePeerHandler extends Thread{
	
	private Peer peer = null;
	private Subscriber sender = null;
	private GroupChannel channel = null;
	
	public MessagePeerHandler(byte[] message, Subscriber sender, Peer peer, GroupChannel channel){
		
		this.sender = sender;
		this.peer = peer;
		this.channel = channel;
		
		String content = new String(message);

		int firstSpace = content.indexOf(new String(" "));
		String type = content.substring(0,firstSpace);
	
		
		if(Util.isTopologyMessageType(type)){
			TopologyMessage msg = TopologyMessage.parseMessage(message);
			handleTopologyMessage(msg);
		}
		else if(Util.isProtocolMessageType(type)){
			//ProtocolMessage msg = ProtocolMessage.parseMessage(message, peer.getVersion());
			/*
			 * PROTOCOLO
			 */
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
			//I'm the root
			case ROOT:{
				boolean sameRoot = channel.getMySubscription().equals(msg.getSubscriber1());
				
				if(!sameRoot){
					channel.setRoot(msg.getSubscriber1());
					channel.setParent(msg.getSubscriber1());
					Logs.newTopology("ROOT", msg.getSubscriber1());
					channel.sendMessageToSubscribers(msg);
					Logs.sentTopologyMessage(msg);
				}
				
				break;
			}
			//Your parent
			case PARENT:{
				TopologyMessage warnMessage;
				
				//If the root  has a parent -> parent is the new root
				if(channel.iAmRoot()){
					warnMessage = new TopologyMessage(Util.TopologyMessageType.ROOT,msg.getSubscriber1());
					channel.sendMessageToRoot(warnMessage);
					Logs.sentTopologyMessage(warnMessage);
					
					channel.setRoot(msg.getSubscriber1());
					Logs.newTopology("ROOT", msg.getSubscriber1());
				}
				//If it already had a parent -> update new parent and warn the old parent to remove me from his childs
				else if(channel.hasParent() && !channel.getParent().equals(msg.getSubscriber1())){
					warnMessage = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,peer.getMySubscriptionInfo());
					channel.sendMessageToParent(warnMessage);
					Logs.sentTopologyMessage(warnMessage);
				}

				//update parent
				channel.setParent(msg.getSubscriber1());
				Logs.newTopology("PARENT",msg.getSubscriber1());
				
				//warn that i'm his subscriber
				warnMessage = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,peer.getMySubscriptionInfo());
				channel.sendMessageToParent(warnMessage);
				Logs.sentTopologyMessage(warnMessage);

				break;
			}
			//I'm your subscriber
			case SUBSCRIBER:{
				if(channel.addSubscriber(msg.getSubscriber1()))
					Logs.newTopology("SUBSCRIBER", msg.getSubscriber1());
				break;
			}
			//Remove me
			case REMSUBSCRIBER:{
				channel.removeSubscriber(msg.getSubscriber1());
				Logs.remTopology("SUBSCRIBER", msg.getSubscriber1());
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
		
		if(msg.getType().compareTo(Util.ActivityMessageType.ACTIVITY) == 0){
			ActivityMessage message = new ActivityMessage(Util.ActivityMessageType.ONLINE);
			channel.sendMessageToTracker(message);
		}
		
	}

}
