package network;
import java.util.ArrayList;

import message.Message;
import peer.Peer;

public class GroupChannel extends Thread{

	private Subscriber tracker = null;
	private Subscriber root = null;
	private Subscriber parent = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();	//max size = 5
	private DatagramListener comunicationChannel = null;

	public GroupChannel(Peer peer, Subscriber tracker){
		this.comunicationChannel = new DatagramListener(peer,this);
		this.tracker = tracker;
		this.root = peer.getMySubscriptionInfo();			
		this.mySubscription = peer.getMySubscriptionInfo();
		comunicationChannel.start();
	}
	
	/*
	 * Information Flow Functions
	 */
	
	public void sendPrivateMessage(Message message, Subscriber destination){
		comunicationChannel.send(message.buildMessage(), destination.getAddress(), destination.getPort());
	}
	
	public void sendMessageToTracker(Message message){
		comunicationChannel.send(message.buildMessage(), tracker.getAddress(), tracker.getPort());
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
	
	public void addSubscriber(Subscriber newSubscriber){
		if(hasSubscriber(newSubscriber) == null){
			nextSubscribers.add(newSubscriber);
		}
	}
	
	public void removeSubscriber(Subscriber subscriber){
		Subscriber p;
		if((p = hasSubscriber(subscriber)) != null)
			nextSubscribers.remove(p);
	}
	
	public Subscriber hasSubscriber(Subscriber subscriber){
		for(Subscriber s : nextSubscribers){
			if(s == subscriber)
				return s;
		}
		return null;
	}
	
	/*
	 * GETS & SETS
	 */
	
	public void setParent(Subscriber subscriber){
		parent = subscriber;
	}
	
	public void setRoot(Subscriber subscriber){
		root = subscriber;
	}
	
	public Subscriber getRoot(){
		return root;
	}
	
	public boolean hasParent(){
		return (parent != null);
	}
	
	public boolean iAmRoot(){
		return (mySubscription.equals(root));
	}
}
