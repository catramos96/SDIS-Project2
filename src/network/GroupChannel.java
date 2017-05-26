package network;
import message.Message;
import message.TopologyMessage;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util;
import java.util.ArrayList;

public class GroupChannel extends Thread{

	private Subscriber tracker = null;
	private Subscriber mySubscription = null;
	private ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
	
	private DatagramListener topChannel = null;			//For Topology/Activity
	private DatagramListener mcChannel = null;			//For Protocol
	private DatagramListener mdrChannel = null;
	private DatagramListener mdbChannel = null;


	public GroupChannel(Peer peer, Subscriber tracker){
		
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
		
		Logs.errorMsg(mySubscription.toString());
		
		//Warn Tracker of access
		TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.ONLINE,mySubscription);
		sendMessageToTracker(msg);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		msg = new TopologyMessage(Util.TopologyMessageType.GETONLINE,10);
		sendMessageToTracker(msg);
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
		if(hasSubscriber(newSubscriber) == null){
			subscribers.add(newSubscriber);
			return true;
		}
		return false;
	}
	
	public void removeSubscriber(Subscriber subscriber){
		Subscriber p;
		if((p = hasSubscriber(subscriber)) != null)
			subscribers.remove(p);
	}
	
	public Subscriber hasSubscriber(Subscriber subscriber){
		for(Subscriber s : subscribers){
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
			subscribers.add(s);
		}
	}
}
