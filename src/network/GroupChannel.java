package network;
import message.ActivityMessage;
import message.Message;
import message.TopologyMessage;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;

import javax.annotation.Resource;

public class GroupChannel extends Thread{

	private Subscriber tracker = null;
	private Subscriber root = null;
	private Subscriber parent = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();	//max size = 5
	
	private DatagramListener topChannel = null;			//For Topology/Activity
	private DatagramListener mcChannel = null;			//For Protocol
	private DatagramListener mdrChannel = null;
	private DatagramListener mdbChannel = null;


	public GroupChannel(Peer peer, Subscriber tracker){
		
		this.topChannel = new DatagramListener(peer,this);
		this.topChannel.start();

		this.mcChannel = new DatagramListener(peer, this);
		this.mcChannel.start();

		this.mdrChannel = new DatagramListener(peer,this);
		this.mdrChannel.start();

		this.mdbChannel = new DatagramListener(peer, this);
		this.mdbChannel.start();
		
		this.tracker = tracker;
		
		peer.getMySubscriptionInfo().setPorts(topChannel.getSocketPort(),mcChannel.getSocketPort(),mdrChannel.getSocketPort(),mdbChannel.getSocketPort());
		this.mySubscription = peer.getMySubscriptionInfo();
		this.root = peer.getMySubscriptionInfo();		
		
		Logs.errorMsg(mySubscription.getSubscriberInfo());
		
		//Ask tracker to be added
		TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.NEWSUBSCRIBER,mySubscription);
		
		sendMessageToTracker(msg);
		
		//Action before logout
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				ActivityMessage activity = new ActivityMessage(Util.ActivityMessageType.OFFLINE,mySubscription);
				sendMessageToTracker(activity);
			}
		});
	}
	
	/*
	 * Information Flow Functions
	 */
	
	//Topology Channel
	
	public void sendMessageToTracker(Message message){
		topChannel.send(message.buildMessage(), tracker.getAddress(), tracker.getDefPort());
	}
	
	//All Channels
	
	public void sendPrivateMessage(Message message, Subscriber destination, Util.ChannelType type){
		DatagramListener channel = getChannel(type);
		int port = destination.getPort(type);
		
		if(port != -1 && channel != null)
			channel.send(message.buildMessage(), destination.getAddress(), port);
		else
			Logs.errorMsg("Could not send message because port or channel not found!");
	}	
	
	public void sendMessageToSubscribers(Message message, Util.ChannelType type){
		
		DatagramListener channel = getChannel(type);
		int port;
		
		for(Subscriber subscriber : nextSubscribers){
			port = subscriber.getPort(type);
			
			if(port != -1 && channel != null)
				channel.send(message.buildMessage(), subscriber.getAddress(), port);
			else{
				Logs.errorMsg("Could not send message because port or channel not found!");
				break;
			}
		}
	}
	
	public void sendMessageToRoot(Message message, Util.ChannelType type){
		DatagramListener channel = getChannel(type);
		int port = root.getPort(type);
		
		if(port != -1 && channel != null)
			channel.send(message.buildMessage(), root.getAddress(), port);
		else
			Logs.errorMsg("Could not send message because port or channel not found!");
	}
	
	public void sendMessageToParent(Message message, Util.ChannelType type){
		DatagramListener channel = getChannel(type);
		int port = parent.getPort(type);
		
		if(port != -1 && channel != null)
			channel.send(message.buildMessage(), parent.getAddress(), port);
		else
			Logs.errorMsg("Could not send message because port or channel not found!");
		
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
	
	// NEED FIX
	
    public void addBackupInitiator(String chunkKey, ChunkBackupProtocol backup) {
       /* comunicationChannel.addBackupInitiator(chunkKey,backup);*/
    }

    public void removeBackupInitiator(String chunkKey){
      /* comunicationChannel.removeBackupInitiator(chunkKey);*/
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
	
	public DatagramListener getChannel(Util.ChannelType type){
		switch (type) {
		case TOP:
			return topChannel;
		case MC:
			return mcChannel;
		case MDR:
			return mdrChannel;
		case MDB:
			return mdbChannel;
		default:
			break;
		}
		return null;
	}
}
