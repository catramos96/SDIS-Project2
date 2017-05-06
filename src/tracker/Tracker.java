package tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resources;

import message.ActivityMessage;
import message.TopologyMessage;
import network.DatagramListener;
import network.Subscriber;
import resources.Logs;
import resources.Util;

public class Tracker{
	
	private Subscriber root = null;
	private LinkedHashMap<Subscriber,ArrayList<Subscriber>> topology = null; //ordered by insertion time
	private HashMap<Subscriber,Boolean> activity = null;
	private HashMap<Subscriber,Subscriber> parentage = null;
	private DatagramListener channel = null;
	
	//to check activity 
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public Tracker(int port){
	
		try {
			this.topology = new LinkedHashMap<Subscriber,ArrayList<Subscriber>>();
			this.activity = new HashMap<Subscriber,Boolean>();
			this.parentage = new HashMap<Subscriber,Subscriber>();
			this.channel = new DatagramListener(this,port);
			
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
			
			final Runnable checkActivity = new Runnable() {
				public void run() { 
					for(Map.Entry<Subscriber, Boolean> entry : activity.entrySet()){
						if(!entry.getValue())
							subscriberOffline(entry.getKey());
						else{
							subscriberOnline(entry.getKey());
						}
					}
				}
			};
			scheduler.scheduleAtFixedRate(checkActivity, Util.CHECK_ACTV_TIME, Util.CHECK_ACTV_TIME, TimeUnit.SECONDS);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * TOPOLOGY
	 */
	
	public synchronized Subscriber addToTopology(Subscriber newSubscriber){

		activity.put(newSubscriber, true);
		
		Map.Entry<Subscriber,ArrayList<Subscriber>> tmpMap;
		
		Subscriber parent = null;
		tmpMap = getFirstFree(newSubscriber);
		
		if(topology.size() > 0){
			parent = tmpMap.getKey();
			ArrayList<Subscriber> childs = tmpMap.getValue();
			childs.add(newSubscriber);
			topology.put(parent, childs);
			parentage.put(newSubscriber, parent);
		}
		
		topology.put(newSubscriber, new ArrayList<Subscriber>());
		return parent;
	}
	
	public synchronized Map.Entry<Subscriber,ArrayList<Subscriber>> getFirstFree(Subscriber s){
		
		for(Map.Entry<Subscriber,ArrayList<Subscriber>> entry : topology.entrySet()){
			if(entry.getValue().size() < Util.MAX_SUBSCRIBERS_CHILDS && entry.getKey() != s){
				return entry;
			}
		}
		return null;
	}
	
	public synchronized ArrayList<Subscriber> getNextSubscribers(Subscriber parent){
		return topology.get(parent);
	}
	
	/*
	 * ACTIVITY
	 */
	
	public synchronized void setSubscriberActivity(Subscriber subscriber, boolean active){
		activity.put(subscriber, active);
	}
	
	public synchronized void subscriberOnline(Subscriber subscriber){
		new Thread(new Runnable(){

			@Override
			public void run() {
				setSubscriberActivity(subscriber, false);
				ActivityMessage message = new ActivityMessage(Util.ActivityMessageType.ACTIVITY);
				channel.send(message.buildMessage(), subscriber.getAddress(), subscriber.getPort());
			}
			
		}).start();
	}
	
	public synchronized void subscriberOffline(Subscriber subscriber){
		new Thread(new Runnable(){

			@Override
			public void run() {
				ArrayList<Subscriber> nextSubscribers = getNextSubscribers(subscriber);
				boolean firstSub = true;
				
				TopologyMessage rootMessage = null;
				
				for(Subscriber s : nextSubscribers){
					//Find a new root if the peer who logged out was the root
					if(subscriber.equals(root) && firstSub){
						setRoot(s);
						Logs.newTopology("ROOT", s);
						rootMessage = new TopologyMessage(Util.TopologyMessageType.ROOT,s);
					}
					//New parents for the children
					else if(!subscriber.equals(root)){
						Subscriber newParent = addToTopology(s);
						TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.PARENT,newParent);
						channel.send(message.buildMessage(), s.getAddress(), s.getPort());
					}
					
					//Send the new root for all the pending peers
					if(rootMessage != null)
						channel.send(rootMessage.buildMessage(), s.getAddress(), s.getPort());
				}
				
				Subscriber parent = getParent(subscriber);
				
				if(parent != null){
					//Warn parent of the peer who logged out
					TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,subscriber);
					channel.send(message.buildMessage(), parent.getAddress(), parent.getPort());
					
					//remove subscriber in the parent childs
					ArrayList<Subscriber> parentChilds = topology.get(parent);
					parentChilds.remove(subscriber);
				}
			}
		}).start();
	}

	
	/*
	 * GETS & SETS
	 */
	
	public DatagramListener getChannel(){
		return this.channel;
	}
	
	public Subscriber getRoot(){
		return root;
	}
	
	public synchronized void setRoot(Subscriber root){
		this.root = root;
	}
	
	public synchronized Subscriber getParent(Subscriber child){
		return parentage.get(child);
	}
	
	public synchronized boolean hasSubscriber(Subscriber s){
		return (topology.get(s) != null);
	}

}
