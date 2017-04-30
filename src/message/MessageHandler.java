package message;
import network.GroupChannel;
import network.Subscriber;
import peer.Peer;
import resources.Logs;
import resources.Util;

public class MessageHandler extends Thread{
	
	private Peer peer = null;
	private GroupChannel channel = null;
	
	public MessageHandler(byte[] message, Subscriber sender, Peer peer, GroupChannel channel){
		
		this.peer = peer;
		this.channel = channel;
		
		String content = new String(message);

		int firstSpace = content.indexOf(new String(" "));
		String type = content.substring(0,firstSpace);
	
		
		if(Util.isTopologyMessageType(type)){
			TopologyMessage msg = TopologyMessage.parseMessage(message);
			handleTopologyMessage(msg,sender);
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
	
	public void handleTopologyMessage(TopologyMessage msg, Subscriber sender){
	
		switch (msg.getType()) {
			//Who is the root ?
			case WHOISROOT:{
				//answer
				TopologyMessage message = null;
				if(channel.getRoot() == null)
					 message = new TopologyMessage(Util.TopologyMessageType.ROOT,peer.getMySubscriptionInfo());
				else
					message = new TopologyMessage(Util.TopologyMessageType.ROOT,channel.getRoot());
				channel.sendPrivateMessage(message, sender);
				Logs.whoIsRootMessage();
				break;
			}
			//I'm the root
			case ROOT:{
				channel.setRoot(msg.getSubscriber1());
				Logs.newRoot(msg.getSubscriber1());
				
				if(channel.isWaitingToBeAdded()){
					TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.NEWSUBSCRIBER,peer.getMySubscriptionInfo());
					channel.sendMessageToRoot(message);
				}
				
				//pass the message to the others subscribers in the group
				channel.sendMessageToSubscribers(msg);
				break;
			}
			//I'm your parent
			case PARENT:{
				if(channel.hasParent()){
					TopologyMessage warnMessage = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,peer.getMySubscriptionInfo());
					channel.sendMessageToParent(warnMessage);
				}
				channel.setParent(msg.getSubscriber1());
				Logs.newParent(msg.getSubscriber1());
				TopologyMessage warnMessage = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,peer.getMySubscriptionInfo());
				channel.sendMessageToParent(warnMessage);
				break;
			}
			//I'm your subscriber
			case SUBSCRIBER:{
				/*
				 * CENAS
				 */
				Logs.yourSubscriber(msg.getSubscriber1());
				break;
			}
			//I'm new Try to add me
			case NEWSUBSCRIBER:{
				if(channel.addSubscriber(msg.getSubscriber1())){	
					TopologyMessage newMsg = new TopologyMessage(Util.TopologyMessageType.PARENT,peer.getMySubscriptionInfo());
					channel.sendPrivateMessage(newMsg, msg.getSubscriber1());
				}
				else{
					/*
					 * MANDAR PARA OUTROS 
					 */
				}
				Logs.newSubscriber(msg.getSubscriber1());
				break;
			}
			//Remove me
			case REMSUBSCRIBER:{
				Logs.removeSubscriber(msg.getSubscriber1());
				channel.removeSubscriber(msg.getSubscriber1());
				break;
			}
			case MOVSUBSCRIBER:{
				System.out.println("RECEIVED MOVSUBSCRIBER MSG");
				break;
			}
			default:{
				System.out.println("RECEIVED UNKNOWN MSG");
				break;
			}
		 }
		
	}

}
