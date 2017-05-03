package tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import network.DatagramListener;
import network.Subscriber;
import resources.Util;

public class Tracker{
	
	private Subscriber root = null;
	private LinkedHashMap<Subscriber,ArrayList<Subscriber>> topology = null; //ordered by insertion time
	private HashMap<Subscriber,Boolean> activity = null;
	private DatagramListener channel = null;
	
	public Tracker(int port){
		this.topology = new LinkedHashMap<Subscriber,ArrayList<Subscriber>>();
		this.activity = new HashMap<Subscriber,Boolean>();
		this.channel = new DatagramListener(this,port);
		
		this.channel.start();
	}
	
	/*
	 * TOPOLOGY
	 */
	
	public Subscriber addToTopology(Subscriber newSubscriber){

		activity.put(newSubscriber, true);
		
		Map.Entry<Subscriber,ArrayList<Subscriber>> tmpMap;
		
		Subscriber free = null;
		tmpMap = getFirstFree(newSubscriber);
		
		if(topology.size() > 0){
		free = tmpMap.getKey();
		ArrayList<Subscriber> next = tmpMap.getValue();
		next.add(newSubscriber);
		topology.put(free, next);
		}
		
		topology.put(newSubscriber, new ArrayList<Subscriber>());
		return free;
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
	
	public synchronized void removeActivitySubscriber(Subscriber subscriber){
		activity.remove(subscriber);
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
	
	

}
