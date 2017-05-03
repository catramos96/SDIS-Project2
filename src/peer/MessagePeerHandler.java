package peer;
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
		else{
			System.out.println(content);
		}
			
	}
	
	public void handleTopologyMessage(TopologyMessage msg){
	
		switch (msg.getType()) {
			//I'm the root
			case ROOT:{
				channel.setRoot(msg.getSubscriber1());
				Logs.newRoot(msg.getSubscriber1());
				
				if(!channel.hasParent()){
					TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.NEWSUBSCRIBER,peer.getMySubscriptionInfo());
					channel.sendMessageToTracker(message);
				}
				else
					channel.sendMessageToSubscribers(msg);
				break;
			}
			//I'm your parent
			case PARENT:{
				Logs.newParent(msg.getSubscriber1());
				TopologyMessage warnMessage;
				
				if(channel.hasParent()){
					warnMessage = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,peer.getMySubscriptionInfo());
					channel.sendMessageToParent(warnMessage);
				}
				
				channel.setParent(msg.getSubscriber1());
				
				warnMessage = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,peer.getMySubscriptionInfo());
				channel.sendMessageToParent(warnMessage);
				break;
			}
			//I'm your subscriber
			case SUBSCRIBER:{
				Logs.yourSubscriber(msg.getSubscriber1());
				channel.addSubscriber(msg.getSubscriber1());				
				break;
			}
			//Remove me
			case REMSUBSCRIBER:{
				Logs.removeSubscriber(msg.getSubscriber1());
				channel.removeSubscriber(msg.getSubscriber1());
				break;
			}
			default:{
				System.out.println("RECEIVED UNKNOWN MSG");
				break;
			}
		 }
		
	}

}
