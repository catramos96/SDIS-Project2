package network;
import message.ActivityMessage;
import message.Message;
import message.TopologyMessage;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;

public class GroupChannel extends Thread{

	private Subscriber tracker = null;
	private Subscriber root = null;
	private Subscriber parent = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();	//max size = 5
	private DatagramListener communicationChannel = null;

	public GroupChannel(Peer peer, Subscriber tracker){
		this.communicationChannel = new DatagramListener(peer,this);
		this.tracker = tracker;
		this.root = peer.getMySubscriptionInfo();			
		this.mySubscription = peer.getMySubscriptionInfo();
		communicationChannel.start();
		
		//Ask tracker to be added
		TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.NEWSUBSCRIBER,mySubscription);
		sendMessageToTracker(msg);
		Logs.sentTopologyMessage(msg);
		
		//Action before logout
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				ActivityMessage activity = new ActivityMessage(Util.ActivityMessageType.OFFLINE);
				sendMessageToTracker(activity);
			}
		});
	}
	
	/*
	 * Information Flow Functions
	 */
	
	public void sendPrivateMessage(Message message, Subscriber destination){
		communicationChannel.send(message.buildMessage(), destination.getAddress(), destination.getPort());
	}
	
	public void sendMessageToTracker(Message message){
		communicationChannel.send(message.buildMessage(), tracker.getAddress(), tracker.getPort());
	}
	
	public void sendMessageToSubscribers(Message message){
		for(Subscriber subscriber : nextSubscribers){
			communicationChannel.send(message.buildMessage(), subscriber.getAddress(), subscriber.getPort());
		}
	}
	
	public void sendMessageToRoot(Message message){
		communicationChannel.send(message.buildMessage(), root.getAddress(), root.getPort());
	}
	
	public void sendMessageToParent(Message message){
		if(parent != null)
			communicationChannel.send(message.buildMessage(), parent.getAddress(), parent.getPort());
	}
	
	/*
	 * Topology Functions
	 */
	
	public boolean addSubscriber(Subscriber newSubscriber){
		if(hasSubscriber(newSubscriber) == null){
			nextSubscribers.add(newSubscriber);
			return true;
		}
		return false;
	}
	
	public void removeSubscriber(Subscriber subscriber){
		Subscriber p;
		if((p = hasSubscriber(subscriber)) != null)
			nextSubscribers.remove(p);
	}
	
	public Subscriber hasSubscriber(Subscriber subscriber){
		for(Subscriber s : nextSubscribers){
			if(s.equals(subscriber))
				return s;
		}
		return null;
	}

    /*
     * others
     */
    public void addBackupInitiator(String chunkKey, ChunkBackupProtocol backup) {
        communicationChannel.addBackupInitiator(chunkKey,backup);
    }

    public void removeBackupInitiator(String chunkKey) {
    	communicationChannel.removeBackupInitiator(chunkKey);
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
	
	public Subscriber getMySubscription(){
		return mySubscription;
	}
	
	public Subscriber getParent(){
		return parent;
	}
}
