package tracker;

import network.Subscriber;
import resources.Logs;

import java.io.Serializable;
import java.util.*;

public class TrackerData implements Serializable
{
    private static final long serialVersionUID = 1L;
    private HashMap<String,HashSet<Subscriber>> DHT = null;

    public TrackerData()
    {
        DHT = new HashMap<String,HashSet<Subscriber>>();
    }

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
        //Logs.delMsg("DELETE DHT " + DHT.containsKey(key) +  " " + key );
        //Logs.delMsg(DHT.toString());
        if(DHT.containsKey(key)) {
            DHT.remove(key);
            Logs.delMsg("REMOVED Key: " + key);
        }
    }
}
