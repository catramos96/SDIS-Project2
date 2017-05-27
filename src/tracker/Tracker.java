package tracker;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import network.DatagramListener;
import network.Subscriber;
import resources.DLinkedList;
import resources.DLNode;
import resources.Logs;
import security.SSLlistenerServer;

public class Tracker{

	private DLinkedList<Subscriber> lastAccess = null;			//LastAccess Subscribers
	private HashMap<Subscriber,DLNode<Subscriber>> subscribers = null;		//Subscribers and lastAccessPosition
	private HashSet<String>  validIPs = null;
	private DatagramListener channel = null;
	private HashMap<String,HashSet<Subscriber>> DHT = null;
	
	public Tracker(int port) throws ExecutionException, InterruptedException
	{
		try {
			lastAccess = new DLinkedList<Subscriber>();
			subscribers = new HashMap<Subscriber,DLNode<Subscriber>>();
			DHT = new HashMap<String,HashSet<Subscriber>>();
			
			validIPs = new HashSet<String>();
			(new Thread(new SSLlistenerServer(4499,new String[0], this))).start();
			
			this.channel = new DatagramListener(this,port);
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Accesses
	 */

	public synchronized void registerSubscriber(Subscriber newS){
		
		if(subscribers.containsKey(newS)){
			DLNode<Subscriber> lastAccessNode = subscribers.get(newS);
			lastAccess.removeNode(lastAccessNode);						//remove old access
		}
		
		DLNode<Subscriber> newAccessNode = lastAccess.addFirst(newS);
		subscribers.put(newS, newAccessNode);									//updates position in access

		//tmp
		System.out.println(lastAccess.toString());
        Logs.newMsg("ACCESS: " + newS.toString());
    }

	public synchronized ArrayList<Subscriber> getLastAccess(int nSubscribers){
		ArrayList<Subscriber> subs = new ArrayList<Subscriber>();
		int size = 0;
		DLNode<Subscriber> node = lastAccess.getFirst();

		while(size < nSubscribers && node != null){
			subs.add(node.getObject());
			node = node.getNext();
		}

		return subs;
	}

	/*
	 * DHT
	 */

	public synchronized void putDHT(String key, Subscriber s){
		if(DHT.containsKey(key)){
		    DHT.get(key).add(s);
        }
        else{
		    Set<Subscriber> set = Collections.synchronizedSet(new HashSet<Subscriber>());
		    set.add(s);
		    DHT.put(key,new HashSet<Subscriber>(set));

		    Logs.newMsg("Key: " + key + " Peer: " + s.toString());
        }
	}

	public synchronized ArrayList<Subscriber> getDHT(String key, int peersN, int pagination){
        ArrayList<Subscriber> subs = new ArrayList<Subscriber>();
        Iterator<Subscriber> it;

        if(DHT.containsKey(key)){
            it = DHT.get(key).iterator();

            int i = (pagination -1) * peersN;
            int j = 0;
            int w = pagination*peersN ;

            while(it.hasNext()){
                if(j == w)
                    break;

                if(i <= j)
                    subs.add(it.next());
                else
                    it.next();
                j++;
            }

        }

        return subs;
    }

    public synchronized int checkDHT(String key){
	    if(DHT.containsKey(key))
	        return DHT.get(key).size();
	    return 0;
    }

	public synchronized int remSubscriberDHT(String key, Subscriber s){
	    if(DHT.containsKey(key)){
	        HashSet<Subscriber> tmp = DHT.get(key);
	        tmp.remove(s);
	        Logs.remMsg("Key: " + key + " Peer: " + s.toString());
	        return tmp.size();
        }

        return 0;
    }

    public synchronized void deleteDHT(String key){
	    if(DHT.containsKey(key)) {
            DHT.remove(key);
            Logs.delMsg("Key: " + key);
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
