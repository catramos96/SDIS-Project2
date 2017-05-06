package tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
	private HashMap<Subscriber,Subscriber> parentage = null;
	private DatagramListener channel = null;
	
	public Tracker(int port){
	
		try {
			this.topology = new LinkedHashMap<Subscriber,ArrayList<Subscriber>>();
			this.activity = new HashMap<Subscriber,Boolean>();
			this.parentage = new HashMap<Subscriber,Subscriber>();
			this.channel = new DatagramListener(this,port);
			
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		/*TESTE*/	
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TopologyMessage msg = null;
		try {
			msg = new TopologyMessage(Util.TopologyMessageType.ROOT,new Subscriber(InetAddress.getByName(new String("127.0.0.1")),5));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channel.send(msg.buildMessage(), root.getAddress(), root.getPort());*/
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
	
	public synchronized void inactiveSubscriber(Subscriber subscriber){
		activity.remove(subscriber);
		Subscriber parent = getParent(subscriber);
		
		if(parent != null){
			ArrayList<Subscriber> parentChilds = topology.get(parent);
			parentChilds.remove(subscriber);
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

}
