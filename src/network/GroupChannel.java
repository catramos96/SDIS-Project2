package network;
import java.util.ArrayList;

import message.Message;
import peer.Peer;

public class GroupChannel extends Thread{

	private Subscriber root = null;
	private Subscriber parent = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();	//max size = 5
	private DatagramListener comunicationChannel = null;
	
	private boolean waitingToBeAdded;

	public GroupChannel(Peer peer){
		this.waitingToBeAdded =true;

		this.comunicationChannel = new DatagramListener(peer,this);
		this.root = peer.getMySubscriptionInfo();				//tmp
		this.mySubscription = peer.getMySubscriptionInfo();
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
	
	public void setWaitingToBeAdded(boolean w){
		waitingToBeAdded = w;
	}
	
	public boolean isWaitingToBeAdded(){
		return waitingToBeAdded;
	}
	
	public boolean hasParent(){
		return (parent != null);
	}
}
