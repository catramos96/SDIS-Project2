package peer;

import filesystem.ChunkInfo;
import filesystem.Database;
import filesystem.FileManager;
import message.MessageRMI;
import message.TopologyMessage;
import network.ChannelRecord;
import network.DatagramListener;
import network.GroupChannel;
import network.Subscriber;
import protocols.BackupInitiator;
import protocols.DeleteInitiator;
import protocols.RestoreInitiator;
import resources.Logs;
import resources.Util;
import security.Encrypt;
import security.SSLlistenerClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer implements MessageRMI
{
    /*informations*/
    private int ID;
    private FileManager fileManager;
    private Database database;
    private ChannelRecord channelRecord = null;

    /*hashmaps with temporary information about actual backups and restores*/
    private HashMap<String, RestoreInitiator> restoreInitiators;

    /*Communication*/
    private DatagramListener comunicationChannel = null;
    private GroupChannel subscribedGroup = null;
    private Subscriber mySubscription = null;
    private SSLlistenerClient client = null;

    private Encrypt encrypt = null;

    /*Schedule*/
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public Peer(int peer_id, String[] peerInfo, String[] trackerInfo, String remoteObjName){
        this.ID = peer_id;
        this.setFileManager(new FileManager(getID()));
        this.channelRecord = new ChannelRecord();
        this.restoreInitiators = new HashMap<>();

        loadDB();

        try {
            this.encrypt = new Encrypt(this);
        } catch (Exception e) {
            System.out.println("Error: Encrypt module unnable to start");
            e.printStackTrace();
        }


        try {
            client = new SSLlistenerClient("localhost", 4499, new String[0], this); //TODO
            client.start();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            System.out.println("CLIENT: Can't connect to server");
            e1.printStackTrace();
            return;
        } catch (InvalidKeyException e) {
            System.out.println("CLIENT: Invalid key");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("CLIENT: cypher algorithms unavaiable");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("CLIENT: error IO Encrypt module");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //this peer
        try {
            mySubscription = new Subscriber(InetAddress.getLocalHost().getHostAddress(),Integer.parseInt(peerInfo[0]),Integer.parseInt(peerInfo[1]),Integer.parseInt(peerInfo[2]),Integer.parseInt(peerInfo[3]));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //tracker
        Subscriber tracker = new Subscriber(trackerInfo[0],Integer.parseInt(trackerInfo[1]));

        //Group1
        subscribedGroup = new GroupChannel(this,tracker);
        subscribedGroup.start();

        startRMI(remoteObjName);

        Logs.MyAddress(mySubscription);


        /*
         * Routine Tasks
         */
        //save metadata in 90s intervals
        saveMetadata();

        //try to backup chunks with actual replication degree bellow desired
        verifyChunks(this);

        //Warns peers about its activity only if this peer can also store new chunks
        updateStateOnTracker();

        //save metadata when shouts down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    serializeDB();
                } catch (InterruptedException e) {
                    //Logs.exception("addShutdownHook", "Peer", e.toString());
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Runnable executed in 90s interval to save metadata, preventing mapping lost if the server crashes.
     */
    private void saveMetadata() {
        final Runnable saveMetadata = new Runnable() {
            public void run() {
                serializeDB();
            }
        };
        scheduler.scheduleAtFixedRate(saveMetadata, 30, 90, TimeUnit.SECONDS);
    }

    private void updateStateOnTracker(){
        final Runnable saveMetadata = new Runnable() {
            public void run() {
                //Only warns peer of its activity if it can also store more chunks
                if(fileManager.getRemainingSpace() > 0) {
                    TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.ONLINE, mySubscription);
                    subscribedGroup.sendMessageToTracker(msg);
                    Logs.sentTopologyMessage(msg);
                }
            }
        };
        scheduler.scheduleAtFixedRate(saveMetadata, 0, 30, TimeUnit.SECONDS);
    }

    public synchronized void loadDB() {

        this.database = new Database();

        File metadata = new File("../peersDisk/Peer"+ID+"/metadata.ser");

        //file can be loaded
        if(metadata.exists())
        {
            try
            {
                FileInputStream fileIn = new FileInputStream("../peersDisk/Peer"+ID+"/metadata.ser");
                ObjectInputStream in  = new ObjectInputStream(fileIn);
                this.database = (Database) in.readObject();
                in.close();
                fileIn.close();

                Logs.serializeWarn("loaded from",ID);
            }
            catch (FileNotFoundException e) {
                Logs.exception("loadDB", "Peer", e.toString());
                e.printStackTrace();
            }
            catch (IOException e) {
                Logs.exception("loadDB", "Peer", e.toString());
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                Logs.exception("loadDB", "Peer", e.toString());
                e.printStackTrace();
            }

            System.out.println("-------------------------");
            database.display();
            System.out.println("-------------------------");
        }
    }

    /**
     * database Object Serialization
     */
    public synchronized void serializeDB()
    {
        System.out.println("-------------------------");
        database.display();
        System.out.println("-------------------------");

        try
        {
            FileOutputStream fileOut = new FileOutputStream("../peersDisk/Peer"+ID+"/metadata.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(database);
            out.close();
            fileOut.close();
            Logs.serializeWarn("saved in", ID);
        }
        catch (FileNotFoundException e) {
            Logs.exception("serializeDB", "Peer", e.toString());
            e.printStackTrace();
        }
        catch (IOException e) {
            Logs.exception("serializeDB", "Peer", e.toString());
            e.printStackTrace();
        }
    }

    //TODO  mudar para a validade dos chunks
    private void verifyChunks(Peer peer) {
        /**
         * Function that gets all the chunks stored by this peer with the atual replication degree
         * bellow the desired and try to initiate the chunk backup protocol for each chunk after a random time,
         */
        final Runnable checkChunks = new Runnable() {
            public void run()
            {
                System.out.println(" - init chunk update - ");

                ArrayList<ChunkInfo> chunks = database.getSentChunksBellowRepDeg();

                for(ChunkInfo c : chunks) {
                    chunkBackup(c);
                }

                System.out.println(" - update completed - ");
            }

        };

        scheduler.scheduleAtFixedRate(checkChunks, 90, 300, TimeUnit.SECONDS);
    }

    public void chunkBackup(ChunkInfo c) {
        BackupInitiator temp = new BackupInitiator(this,"",0);
        temp.sendChunk(c);
        temp.waitProtocols();
    }

    public GroupChannel getSubscribedGroup(){
        return subscribedGroup;
    }

    public DatagramListener getChannel(){
        return comunicationChannel;
    }

    public Subscriber getMySubscriptionInfo(){
        return mySubscription;
    }

    private void startRMI(String remoteObjectName)
    {
        try {
            MessageRMI stub = (MessageRMI) UnicastRemoteObject.exportObject(this, 0);
            LocateRegistry.getRegistry().rebind(remoteObjectName, stub);
            System.out.println("Server Ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String backup(final String filename, final int repDeg) throws RemoteException
    {
        System.out.println("Backup initiated...");
        new BackupInitiator(this,filename, repDeg).start();
        return null;
    }

    @Override
    public String restore(final String filename) throws RemoteException
    {
        System.out.println("Restore initiated...");
        new RestoreInitiator(this, filename).start();
        return null;
    }

    @Override
    public String delete(final String filename) throws RemoteException
    {
        System.out.println("Delete initiated...");
        new DeleteInitiator(this, filename).start();
        return null;
    }

    @Override
    public String reclaim(final int spaceToReclaim) throws RemoteException
    {
        System.out.println("Reclaim initiated...");
        return null;
    }

    @Override
    public String state() throws RemoteException
    {
        System.out.println("State initiated...");
        return null;
    }

    public FileManager getFileManager()
    {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager)
    {
        this.fileManager = fileManager;
    }

    public int getID()
    {
        return ID;
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }


    public Database getDatabase() {return database;}

    public void setDatabase(final Database database) {this.database = database;}

    public ChannelRecord getChannelRecord() {
        return channelRecord;
    }

    public void addRestoreInitiator(String fileId, RestoreInitiator restore) {
        restoreInitiators.put(fileId, restore);
    }

    public RestoreInitiator getRestoreInitiator(String fileId) {
        return restoreInitiators.get(fileId);
    }

    public void removeRestoreInitiator(String fileId) {
        restoreInitiators.remove(fileId);
    }

    public Encrypt getEncrypt() { return encrypt;}
}
