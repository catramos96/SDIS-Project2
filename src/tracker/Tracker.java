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
	
	private LinkedHashMap<Subscriber,TrackedInfo> topology = null;
	private DatagramListener channel = null;
	
	//to check activity 
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public Tracker(int port) throws ExecutionException, InterruptedException
	{
	
		try {
			this.topology = new LinkedHashMap<Subscriber,TrackedInfo>();
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
				
					LinkedHashMap<Subscriber,TrackedInfo> tmp = (LinkedHashMap<Subscriber, TrackedInfo>) topology.clone();
					System.out.println("==== CHECK ACTIVITY ==== SUBSCRIPTORS: " + topology.size() + " ====");
					
					//Request Activity
					ActivityMessage message = new ActivityMessage(Util.ActivityMessageType.ACTIVITY);
					for(Subscriber s : topology.keySet()){
						channel.send(message.buildMessage(),s.getAddress(), s.getPort());
					}
					
					Thread.sleep(1000); 	//value tmp
					
					//Check Activity
					for(Entry<Subscriber, TrackedInfo> entry : tmp.entrySet()){
						
						TrackedInfo info = entry.getValue();
						Subscriber subscriber = entry.getKey();
						
						printSubscriber(subscriber);
						
						if(!info.active){
							subscriberOffline(subscriber);
				    	}
						else
							info.active = false;
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
		
		if(!topology.containsKey(newSubscriber))
			topology.put(newSubscriber, new TrackedInfo());
		else if(topology.get(newSubscriber).parent != null)
			return topology.get(newSubscriber).parent;
			
		Map.Entry<Subscriber,TrackedInfo> tmpMap = getFirstFree(newSubscriber);
		TrackedInfo info = topology.get(newSubscriber);
		
		if(topology.size() > 0 && tmpMap != null){		
			tmpMap.getValue().childs.add(newSubscriber);
			info.setParent(tmpMap.getKey());
		}
		
		return info.parent;
	}
	
	public synchronized Map.Entry<Subscriber,TrackedInfo> getFirstFree(Subscriber s){
		
		ArrayList<Subscriber> myChilds = topology.get(s).childs;
		
		for(Map.Entry<Subscriber, TrackedInfo> entry : topology.entrySet()){
			Subscriber tmpS = entry.getKey();
			TrackedInfo tmpInfo = entry.getValue();
			
			//next subscribers might be in bottom levels
			if(myChilds.contains(tmpS)){
				return null;
			}
			
			if(tmpInfo.childs.size() < Util.MAX_SUBSCRIBERS_CHILDS && 
				!tmpS.equals(s)){
				return entry;
			}
		}
		
		return null;
	}
	
	public synchronized ArrayList<Subscriber> getNextSubscribers(Subscriber parent){
		return topology.get(parent).childs;
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
		topology.get(subscriber).setActivity(active);
	}
	
	public synchronized void subscriberOffline(Subscriber subscriber){
		TopologyMessage rootMessage = null;
		TrackedInfo info = topology.get(subscriber);
		ArrayList<Subscriber> childs = (ArrayList<Subscriber>) info.childs.clone();
		
		//if has parent
		if(info.parent != null){

			//remove subscriber in the parent childs
			topology.get(info.parent).childs.remove(subscriber);
			
			//Warn parent of the peer who logged out
			TopologyMessage message = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,subscriber);
			channel.send(message.buildMessage(), info.parent.getAddress(), info.parent.getPort());
			
			//remove parantage entry
			info.setParent(null);
		}

		//remove subscriber from topology
		topology.remove(subscriber);

		//if the root isn't online -> find new root
		Subscriber betterRoot = null;
		if(root.equals(subscriber)){
			
			if(childs.size() == 0){
				root = null;
				return;
			}
			
			betterRoot = chooseBetterForRoot(childs);
			this.root = betterRoot;
			Logs.newTopology("ROOT", betterRoot);

			topology.get(betterRoot).setParent(null);
			
			rootMessage = new TopologyMessage(Util.TopologyMessageType.ROOT,betterRoot);
			channel.send(rootMessage.buildMessage(), betterRoot.getAddress(), betterRoot.getPort());
			
			childs.remove(betterRoot);
		}
		
		for(Subscriber s : childs){

			topology.get(s).setParent(null);
			
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
	
	public synchronized boolean hasSubscriber(Subscriber s){
		return (topology.get(s) != null);
	}
	
	public void printSubscriber(Subscriber s){
		
		TrackedInfo info = topology.get(s);
		
		String print = new String();
		print = "\nS: " + s.getSubscriberInfo() + "   P: ";
		
		if(info.parent != null)
			print += info.parent.getSubscriberInfo() + "   A: " + info.active;
		
		for(Subscriber tmp : info.childs){
			print += "\n          " + tmp.getSubscriberInfo();
		}
		System.out.println(print);
	}

	public TrackedInfo getInfo(Subscriber s){
		return topology.get(s);
	}
}
