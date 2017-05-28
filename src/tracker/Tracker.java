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

/**
 * Class that represents the server that keeps track of the peers in the aplication
 * and the files that where backed up
 */
public class Tracker {
	private DLinkedList<Subscriber> lastAccess = null;			            //LastAccess Subscribers
	private HashMap<Subscriber,DLNode<Subscriber>> subscribers = null;		//Subscribers and Nodes of the DLindedList
	private HashSet<String>  validIPs = null;
	private DatagramListener channel = null;
	private TrackerData trackerData;

    /*Schedule*/
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);


    /**
     * Constructor of the tracker
     * @param port associated to the tracker
     * @throws ExecutionException
     * @throws InterruptedException
     */
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

		showTrackerState();

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

    /**
     * Function that loads the last data saved of the tracker
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
     * Function that shows the tracker state
     */
    public synchronized void showTrackerState(){
        final Runnable showState = new Runnable() {
            public void run() {
                displayState();
            }
        };
        scheduler.scheduleAtFixedRate(showState, 30, 30, TimeUnit.SECONDS);
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

    /**
     * Function that saves the information of the tracker except the lastAccess
     */
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

    /**
     * Function that registes a subscriber
     * @param newS
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

    /**
     * Function that returns the lastAccessed nSubscribers
     * @param nSubscribers
     * @return
     */
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

    /**
     * Function that returns peersN subscribers that have a chunk with the given key and
     * with pagination n
     * @param key of the chunk
     * @param peersN number of peers to retrieve
     * @param pagination pagination in the query
     * @return Information of the peers that have the chunk
     */
	public synchronized ArrayList<Subscriber> getDHT(String key, int peersN, int pagination){
        return trackerData.getDHT(key,peersN,pagination);
    }

    /**
     * Function that checks a chunk by its key
     * @param key of the chunk
     * @return Replication degree of the chunk
     */
    public synchronized int checkDHT(String key){
	    return trackerData.checkDHT(key);
    }

    /**
     * Function that removes a peer that had a chunk
     * @param key of the chunk
     * @param s peer information
     * @return New replication Degree of Chunk
     */
	public synchronized int remSubscriberDHT(String key, Subscriber s){
	    return trackerData.remSubscriberDHT(key,s);
    }

    /**
     * Function that deletes a chunk entry by its key
     * @param key
     */
    public synchronized void deleteDHT(String key){
    	trackerData.deleteDHT(key);
    }
	
	/*
	 * GETS & SETS
	 */

    /**
     * Function that returns the tracker's channel
     * @return DatagramListener
     */
	public DatagramListener getChannel(){
		return this.channel;
	}

    /**
     * Function that adds a new Ip address to the validated Ips
     * @param ip
     */
	public void addIP(InetAddress ip) {
		validIPs.add(ip.getHostAddress());
	}

    /**
     * Function that checks if a given ip addresss is authorized
     * @param ip
     * @return
     */
	public boolean authorizedIP (String ip) {
		return validIPs.contains(ip);
	}

    /**
     * Function that displays the tracker Information: lastAccess Peers and Chunks in Backup
     */
    public  synchronized  void displayState(){
	    String s = new String("");

	    s += "============================================================\n" +
             "=                          TRACKER                         =\n" +
             "============================================================\n\n";

	    s+= "LAST ACCESS: \n\n" +
            lastAccess.toString() + "\n\n";

	    s+= "DISTRIBUTED HASH TABLE FOR FILES STORED: \n\n" +
                trackerData.toString() + "\n\n" +
                "============================================================\n\n";

        System.out.println(s);
    }
}
