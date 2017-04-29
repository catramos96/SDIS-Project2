package network;
import java.net.InetAddress;
import java.util.ArrayList;

import message.Message;
import peer.Peer;

public class GroupChannel extends Thread{
	
	/*
	 * Max children = 3
	 */
	
	private Subscriber root = null;
	private Subscriber parent = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();
	private DatagramListener comunicationChannel = null;

	public GroupChannel(InetAddress rootAddress, int rootPort, Peer peer){
		comunicationChannel = new DatagramListener(peer,peer.getMySubscriptionInfo().getPort());
		
		root = new Subscriber(rootAddress,rootPort);
		mySubscription = peer.getMySubscriptionInfo();
		
		comunicationChannel.start();
	}
	
	/*
	 * Information Flow Functions
	 */
	
	public void sendPrivateMessage(Message message, Subscriber destination){
		comunicationChannel.send(message.buildMessage(), destination.getAddress(), destination.getPort());
	}
	
	public void sendMessageToSubscribers(Message message){
		for(Subscriber subscriber : nextSubscribers){
			comunicationChannel.send(message.buildMessage(), subscriber.getAddress(), subscriber.getPort());
		}
	}
	
	public void sendMessageToRoot(Message message){
		comunicationChannel.send(message.buildMessage(), root.getAddress(), root.getPort());
	}
	
	public void sendMessageToParent(Message message){
		if(parent != null)
			comunicationChannel.send(message.buildMessage(), parent.getAddress(), parent.getPort());
	}
	
	/*
	 * Topology Functions
	 */
	
	public boolean addSubscriber(Subscriber newSubscriber){
		if(hasSubscriber(newSubscriber) == null){
			if(nextSubscribers.size() < 5){
				nextSubscribers.add(newSubscriber);
				return true;
			}
			else
				return false;
		}
		return true;
	}
	
	public void removeSubscriber(Subscriber subscriber){
		Subscriber p;
		if((p = hasSubscriber(subscriber)) != null)
			nextSubscribers.remove(p);
	}
	
	public Subscriber hasSubscriber(Subscriber subscriber){
		for(Subscriber s : nextSubscribers){
			if(s.equal(subscriber))
				return s;
		}
		return null;
	}
	
	public void setParent(Subscriber subscriber){
		if(parent != null){
			parent.setSubscriber(subscriber.getAddress(), subscriber.getPort());
		}
		else{
			parent = new Subscriber(subscriber.getAddress(), subscriber.getPort());
		}
	}
	
	public void setRoot(Subscriber subscriber){
		root.setSubscriber(subscriber.getAddress(),subscriber.getPort());
	}
}
