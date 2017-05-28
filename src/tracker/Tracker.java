package tracker;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import network.DatagramListener;
import network.Subscriber;
import resources.DLinkedList;
import resources.DLNode;
import resources.Logs;
import security.SSLlistenerServer;

public class Tracker {

	private DLinkedList<Subscriber> lastAccess = null;			//LastAccess Subscribers
	private HashMap<Subscriber,DLNode<Subscriber>> subscribers = null;		//Subscribers and lastAccessPosition
	private HashSet<String>  validIPs = null;
	private DatagramListener channel = null;
	private TrackerData trackerData;

    /*Schedule*/
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);


    public Tracker(int port) throws ExecutionException, InterruptedException
	{
		try {
			lastAccess = new DLinkedList<Subscriber>();
			subscribers = new HashMap<Subscriber,DLNode<Subscriber>>();

			loadTrackerData();

			validIPs = new HashSet<String>();
			(new Thread(new SSLlistenerServer(4499,new String[0], this))).start();
			
			this.channel = new DatagramListener(this,port);
			this.channel.start();
			
			System.out.println("TRACKER: <" + InetAddress.getLocalHost().getHostAddress() + ":" + port + ">");
		
		} catch (IOException e) {
			e.printStackTrace();
		}

        saveTrackerData();

        //save metadata when shouts down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    serializeTrackerData();
                } catch (InterruptedException e) {
                    //Logs.exception("addShutdownHook", "Peer", e.toString());
                    e.printStackTrace();
                }
            }
        });
	}

    /*
     * METADATA
     */

    public synchronized void loadTrackerData() {

        this.trackerData = new TrackerData();

        File metadata = new File("../tracker.ser");

        //file can be loaded
        if(metadata.exists())
        {
            try
            {
                FileInputStream fileIn = new FileInputStream("../tracker.ser");
                ObjectInputStream in  = new ObjectInputStream(fileIn);
                this.trackerData = (TrackerData) in.readObject();
                in.close();
                fileIn.close();

                Logs.serializeWarn("loaded from tracker.ser",0);
            }
            catch (FileNotFoundException e) {
                Logs.exception("loadTrackerData", "Tracker", e.toString());
                e.printStackTrace();
            }
            catch (IOException e) {
                Logs.exception("loadTrackerData", "Tracker", e.toString());
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                Logs.exception("loadTrackerData", "Tracker", e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Runnable executed in 1h interval to save metadata, preventing mapping lost if the server crashes.
     */
    private void saveTrackerData() {
        final Runnable saveMetadata = new Runnable() {
            public void run() {
                serializeTrackerData();
            }
        };
        scheduler.scheduleAtFixedRate(saveMetadata, 15, 60, TimeUnit.MINUTES);
    }

    public synchronized void serializeTrackerData()
    {
        try
        {
            FileOutputStream fileOut = new FileOutputStream("../tracker.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(trackerData);
            out.close();
            fileOut.close();
            Logs.serializeWarn("saved in tracker.ser", 0);
        }
        catch (FileNotFoundException e) {
            Logs.exception("serializeTrackerData", "Tracker", e.toString());
            e.printStackTrace();
        }
        catch (IOException e) {
            Logs.exception("serializeTrackerData", "Tracker", e.toString());
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
		trackerData.putDHT(key,s);
	}

	public synchronized ArrayList<Subscriber> getDHT(String key, int peersN, int pagination){
        return trackerData.getDHT(key,peersN,pagination);
    }

    public synchronized int checkDHT(String key){
	    return trackerData.checkDHT(key);
    }

	public synchronized int remSubscriberDHT(String key, Subscriber s){
	    return trackerData.remSubscriberDHT(key,s);
    }

    public synchronized void deleteDHT(String key){
    	trackerData.deleteDHT(key);
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
