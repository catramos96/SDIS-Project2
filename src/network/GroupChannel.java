package network;
import message.Message;
import message.TopologyMessage;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util;

import java.util.*;

public class GroupChannel extends Thread{

	private Subscriber tracker = null;                  //Tracker info
	private Subscriber mySubscription = null;           //My info
	private Set<Subscriber> subscribers = null;         //My group of subscribers
	
	private DatagramListener topChannel = null;			//For Topology Messages
	private DatagramListener mcChannel = null;			//For Protocol Messages
	private DatagramListener mdrChannel = null;         //For Protocol Messages: CHUNK
	private DatagramListener mdbChannel = null;         //For Protocol Messages: STORE


	public GroupChannel(Peer peer, Subscriber tracker){

	    subscribers = Collections.synchronizedSet(new HashSet<Subscriber>());

		this.topChannel = new DatagramListener(peer,this,Util.ChannelType.TOP);
		this.topChannel.start();

		this.mcChannel = new DatagramListener(peer, this,Util.ChannelType.MC);
		this.mcChannel.start();

		this.mdrChannel = new DatagramListener(peer,this,Util.ChannelType.MDR);
		this.mdrChannel.start();

		this.mdbChannel = new DatagramListener(peer, this,Util.ChannelType.MDB);
		this.mdbChannel.start();
		
		this.tracker = tracker;
		
		peer.getMySubscriptionInfo().setPorts(topChannel.getSocketPort(),mcChannel.getSocketPort(),mdrChannel.getSocketPort(),mdbChannel.getSocketPort());
		this.mySubscription = peer.getMySubscriptionInfo();

		//=================  PARA REMOVER
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        //=================
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
		
		for(Subscriber subscriber : subscribers){
			port = subscriber.getPort(type);
			
			if(port != -1 && channel != null)
				channel.send(message.buildMessage(), subscriber.getAddress(), port);
			else{
				Logs.errorMsg("Could not send message because port or channel not found!");
				break;
			}
		}
	}
	
	/*
	 * Topology Functions
	 */
	
	public boolean addSubscriber(Subscriber newSubscriber){
		return subscribers.add(newSubscriber);
	}
	
	public boolean removeSubscriber(Subscriber subscriber){
		return subscribers.remove(subscriber);
	}
	
	public boolean hasSubscriber(Subscriber subscriber){
		return subscribers.contains(subscriber);
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

	public void resetSubscribers(){
		subscribers.clear();
	}

	public Subscriber getMySubscription(){
		return mySubscription;
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

	public void addSubscribers(ArrayList<Subscriber> subs){
		for(Subscriber s : subs){
		    if(!s.equals(mySubscription))
			subscribers.add(s);
		}
	}
}
