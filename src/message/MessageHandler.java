package message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Subscriber;
import peer.Peer;
import resources.Logs;
import resources.Util;

public class MessageHandler extends Thread{
	
	private Peer peer = null;
	
	public MessageHandler(byte[] message, Peer peer){
		
		this.peer = peer;
		
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
				Subscriber root = new Subscriber(msg.getSubscriberAddress(),msg.getSubscriberPort());
				peer.getSubscribedGroup().setRoot(root);
				Logs.newRoot(root);
				break;
			}
			//I'm your parent
			case PARENT:{
				Subscriber parent = new Subscriber(msg.getSubscriberAddress(),msg.getSubscriberPort());
				peer.getSubscribedGroup().setParent(parent);
				Logs.newParent(parent);
				break;
			}
			//I'm your subscriber
			case SUBSCRIBER:{
				Subscriber son = new Subscriber(msg.getSubscriberAddress(),msg.getSubscriberPort());
				Logs.newSubscriber(son);
				break;
			}
			//I'm new Try to add me
			case NEWSUBSCRIBER:{
				Subscriber newSubscriber = new Subscriber(msg.getSubscriberAddress(),msg.getSubscriberPort());
				if(peer.getSubscribedGroup().addSubscriber(newSubscriber)){
					
					String myAddress = peer.getMySubscriptionInfo().getAddress().getHostAddress();
					int myPort = peer.getMySubscriptionInfo().getPort();
					
					TopologyMessage newMsg = new TopologyMessage(Util.TopologyMessageType.PARENT,myAddress,myPort);
					peer.getSubscribedGroup().sendPrivateMessage(newMsg, newSubscriber);
				}
				else{
					/*
					 * Avisar o parent para ir para o próximo irmão e só depois
					 * Avisar os nextSubscribers
					 */
				}
				break;
			}
			//Remove me
			case REMSUBSCRIBER:{
				System.out.println("RECEIVED REMSUBSCRIBER MSG");
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
