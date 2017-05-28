package tracker;

import network.Subscriber;
import resources.Logs;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Serializable class to save the information about the files in backup
 */
public class TrackerData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private HashMap<String,HashSet<Subscriber>> DHT = null;

    /**
     * Constructor that initializes the DHT
     */
    public TrackerData()
    {
        DHT = new HashMap<String,HashSet<Subscriber>>();
    }

    /**
     * Function that inserts a new subscriber that has saved the chunk of a backup
     * @param key of the chunk
     * @param s information of the peer
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

    /**
     * Function that returns peersN Subscribers holding a chunk with key
     * and with pagination p
     * @param key of chunk
     * @param peersN number of peers to return
     * @param pagination of query
     * @return
     */
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

    /**
     * Function that checks a chunk with key and returns the number os Subscribers in the HashSet of that ket
     * @param key of chunk
     * @return Number of peers in the hashSet
     */
    public synchronized int checkDHT(String key){
        if(DHT.containsKey(key))
            return DHT.get(key).size();
        return 0;
    }

    /**
     * Function that removes a subscriber from the chunk key entry
     * @param key of chunk
     * @param s subscriber to remove
     * @return Number of chunks that remained
     */
    public synchronized int remSubscriberDHT(String key, Subscriber s){
    	if(DHT.containsKey(key)){
	        HashSet<Subscriber> tmp = DHT.get(key);
	        tmp.remove(s);
	        if(tmp.size() < 1) {
	        	DHT.remove(key);
	        }
	        Logs.remMsg("Key: " + key + " Peer: " + s.toString());
	        return tmp.size();
        }

        return 0;
    }

    /**
     * Function that deletes de entry key of a chunk
     * @param key of the chunk
     */
    public synchronized void deleteDHT(String key){
        if(DHT.containsKey(key)) {
            DHT.remove(key);
            Logs.delMsg("REMOVED Key: " + key);
        }
    }

    @Override
    public synchronized String toString(){
        String s = new String("");

        for(Entry<String, HashSet<Subscriber>> e : DHT.entrySet()){
            s += e.getKey() + "\n";

            for(Subscriber sub : e.getValue()){
                s+= "        " + sub.toString() + "\n";
            }
        }
        return s;
    }
}
