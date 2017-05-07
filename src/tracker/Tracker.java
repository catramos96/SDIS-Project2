package tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	
	public Tracker(int port) throws ExecutionException, InterruptedException
	{
	
		try {
			this.topology = new LinkedHashMap<Subscriber,ArrayList<Subscriber>>();
			this.activity = new HashMap<Subscriber,Boolean>();
			this.parentage = new HashMap<Subscriber,Subscriber>();
			this.channel = new DatagramListener(this,port);
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
			
			checkSubscribersActivity();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * TOPOLOGY
	 */
	
	public synchronized void checkSubscribersActivity(){
		final Runnable checkActivity = new  Runnable() {
			public synchronized void run() 
			{ 
				try {
					
				System.out.println("activity");
				
				//Request Activity
				ActivityMessage message = new ActivityMessage(Util.ActivityMessageType.ACTIVITY);
				ArrayList<Subscriber> toRemove = new ArrayList<Subscriber>();
				
				for(Map.Entry<Subscriber, Boolean> entry : activity.entrySet()){
					channel.send(message.buildMessage(), entry.getKey().getAddress(), entry.getKey().getPort());
				}
				
				Thread.sleep(1000); 	//value tmp
				
				//Check Activity
				for(Entry<Subscriber, Boolean> entry : activity.entrySet()){
					
					printSubscriber(entry.getKey());
					
					if(!entry.getValue()){
						subscriberOffline(entry.getKey());
						toRemove.add(entry.getKey());
			    	}
					else
						setSubscriberActivity(entry.getKey(), false);
				}
			    
			    for(Subscriber s : toRemove){
			    	activity.remove(s);
			    }
			    
			    if(topology.size() == 0)
			    	root = null;
			    
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(checkActivity, Util.CHECK_ACTV_TIME, Util.CHECK_ACTV_TIME, TimeUnit.SECONDS);
	}
	
	public synchronized Subscriber addToTopology(Subscriber newSubscriber){
		
		if(!activity.containsKey(newSubscriber))
			activity.put(newSubscriber, false);
		
		if(!topology.containsKey(newSubscriber))
			topology.put(newSubscriber, new ArrayList<Subscriber>());
		
		if(getParent(newSubscriber) != null)
			return getParent(newSubscriber);
			
		Map.Entry<Subscriber,ArrayList<Subscriber>> tmpMap = getFirstFree(newSubscriber);
		Subscriber parent = null;
		
		if(topology.size() > 0 && tmpMap != null){			
			parent = tmpMap.getKey();
			topology.get(parent).add(newSubscriber);
			parentage.put(newSubscriber, parent);
		}
		
		return parent;
	}
	
	public synchronized Map.Entry<Subscriber,ArrayList<Subscriber>> getFirstFree(Subscriber s){
		
		ArrayList<Subscriber> myChilds = new ArrayList<Subscriber>();
		myChilds = topology.get(s);
		
		for(Map.Entry<Subscriber,ArrayList<Subscriber>> entry : topology.entrySet()){
			System.out.println(entry.getKey().getPort());
			
			//next subscribers might be in bottom levels
			if(myChilds.contains(entry.getKey())){
				return null;
			}
			
			if(entry.getValue().size() < Util.MAX_SUBSCRIBERS_CHILDS && 
				!entry.getKey().equals(s)){
				return entry;
			}
		}
		
		return null;
	}
	
	public synchronized ArrayList<Subscriber> getNextSubscribers(Subscriber parent){
		ArrayList<Subscriber> next = new ArrayList<Subscriber>();
		
		if(topology.containsKey(parent))
			next = topology.get(parent);
		
		return next;
	}
	
	
	public synchronized Subscriber chooseBetterForRoot(ArrayList<Subscriber> candidates){
		for(Subscriber s : topology.keySet()){
			if(candidates.contains(s))
				return s;
		}
		return null;
	}
	
	/*
	 * ACTIVITY
	 */
	
	public synchronized void setSubscriberActivity(Subscriber subscriber, boolean active){
		activity.put(subscriber, active);
	}
	
	public synchronized void subscriberOffline(Subscriber subscriber){
		
		ArrayList<Subscriber> nextSubscribers = (ArrayList<Subscriber>) topology.get(subscriber).clone();
		TopologyMessage rootMessage = null;
		Subscriber parent = getParent(subscriber);
		
		//if has parent
		if(parent != null){

			//remove subscriber in the parent childs
			topology.get(parent).remove(subscriber);
			
			//remove parantage entry
			parentage.remove(subscriber);
			
			//Warn parent of the peer who logged out
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,subscriber);
			channel.send(message.buildMessage(), parent.getAddress(), parent.getPort());
		}
		
		//remove subscriber from topology
		topology.remove(subscriber);
		
		//if the root isn't online -> find new root
		Subscriber betterRoot;
		if(root.equals(subscriber)){
			
			if(nextSubscribers.size() == 0){
				root = null;
				return;
			}
			
			betterRoot = chooseBetterForRoot(nextSubscribers);
			
			setRoot(betterRoot);
			Logs.newTopology("ROOT", betterRoot);

			parentage.remove(betterRoot);
			
			rootMessage = new TopologyMessage(Util.TopologyMessageType.ROOT,betterRoot);
			channel.send(rootMessage.buildMessage(), betterRoot.getAddress(), betterRoot.getPort());
			
			nextSubscribers.remove(betterRoot);
		}
		
		for(Subscriber s : nextSubscribers){
			parentage.remove(s);
			Subscriber newParent = addToTopology(s);
			if(newParent != null){
				TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.PARENT,newParent);
				channel.send(message.buildMessage(), s.getAddress(), s.getPort());
			}
			
			//Send the new root for all the pending peers
			if(rootMessage != null)
				channel.send(rootMessage.buildMessage(), s.getAddress(), s.getPort());
		}
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
	
	public void printSubscriber(Subscriber s){
		String print = new String();
		print = "\n" + s.getSubscriberInfo() + "   P: ";
		
		if(parentage.containsKey(s))
			print += getParent(s).getSubscriberInfo() + "   A: " + activity.get(s);
		
		for(Subscriber tmp : topology.get(s)){
			print += "\n          " + tmp.getSubscriberInfo();
		}
		System.out.println(print);
	}

}
