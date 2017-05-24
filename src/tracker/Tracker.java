package tracker;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import network.DatagramListener;
import network.Subscriber;
import resources.DLinkedList;
import resources.DLNode;
import security.SSLlistenerServer;

public class Tracker{
	
	private HashMap<String,ArrayList<Subscriber>> DTH = null;	//Files 
	private DLinkedList<Subscriber> lastAccess = null;			//LastAccess Subscribers
	private HashMap<Subscriber,DLNode<Subscriber>> subscribers = null;		//Subscribers and lastAccessPosition
	private HashSet<String>  validIPs = null;
	private DatagramListener channel = null;
	
	public Tracker(int port) throws ExecutionException, InterruptedException
	{
		try {
			DTH = new HashMap<String,ArrayList<Subscriber>>();
			lastAccess = new DLinkedList<Subscriber>();
			subscribers = new HashMap<Subscriber,DLNode<Subscriber>>();
			
			validIPs = new HashSet<String>();
			(new Thread(new SSLlistenerServer(4499,new String[0], this))).start();
			
			this.channel = new DatagramListener(this,port);
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void registerSubscriber(Subscriber newS){
		
		if(subscribers.containsKey(newS)){
			DLNode<Subscriber> lastAccessNode = subscribers.get(newS);
			lastAccess.removeNode(lastAccessNode);						//remove old access
		}
		
		DLNode<Subscriber> newAccessNode = lastAccess.addFirst(newS);
		subscribers.put(newS, newAccessNode);									//updates position in access
	}
	
	public synchronized void putDTH(String key, Subscriber new_s){
		if(DTH.containsKey(key)){
			ArrayList<Subscriber> subs = DTH.get(key);
			if(!subs.contains(new_s))
				subs.add(new_s);
		}
	}
	
	public synchronized ArrayList<Subscriber> getDTH(String key, int nSubscribers, int pagination){
		ArrayList<Subscriber> subs = new ArrayList<Subscriber>();
		
		if(DTH.containsKey(key)){
			ArrayList<Subscriber> tmp = DTH.get(key);
			int size = tmp.size();
			int i = nSubscribers * pagination, n = 0;
			while(n < nSubscribers && i < size){
				subs.add(tmp.get(i));
				i++;
				n++;
			}
		}
		
		return subs;
	}
	
	public synchronized void removeDTH(String key, Subscriber s){
		if(DTH.containsKey(key)){
			ArrayList<Subscriber> subs = DTH.get(key);
			if(subs.contains(s))
				subs.remove(s);
		}
	}
	
	/*
	 * GETS & SETS
	 */
	
	public DatagramListener getChannel(){
		return this.channel;
	}
	
	
	public void addIP(InetAddress ip) {
		validIPs.add(ip.getHostAddress());
	}
	public void removeIP(String ip) {
		validIPs.remove(ip);
	}
	
	public boolean authorizedIP (String ip) {
		return validIPs.contains(ip);
	}
	
}
