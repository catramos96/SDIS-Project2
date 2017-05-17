package peer;

import filesystem.Database;
import filesystem.FileManager;
import message.MessageRMI;
import network.DatagramListener;
import network.GroupChannel;
import network.MessageRecord;
import network.Subscriber;
import protocols.BackupInitiator;
import protocols.RestoreInitiator;
import resources.Logs;
import security.Encrypt;
import security.SSLlistenerClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.NoSuchPaddingException;

public class Peer implements MessageRMI {
	/*informations*/
	private int ID;
	private FileManager fileManager;
	private Database database;

	/*MessageRecord*/
	private MessageRecord msgRecord = null;

	private HashMap<String, RestoreInitiator> restoreInitiators;

	private DatagramListener comunicationChannel = null;
	private GroupChannel subscribedGroup = null;
	private Subscriber mySubscription = null;
	private SSLlistenerClient client = null;
	private Encrypt encrypt = null;

	public Peer(int peer_id, String[] trackerInfo, String remoteObjName){
		this.ID = peer_id;
		this.setFileManager(new FileManager(getID()));
		this.database = new Database();
		this.msgRecord = new MessageRecord();
		this.restoreInitiators = new HashMap<String, RestoreInitiator>();

		try {
			this.encrypt = new Encrypt(this);
		} catch (Exception e) {
			System.out.println("Error: Encrypt module unnable to start");
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


		try {
			mySubscription = new Subscriber(InetAddress.getLocalHost().getHostAddress(), -1);

			//tracker
			Subscriber tracker = new Subscriber(trackerInfo[0],Integer.parseInt(trackerInfo[1]));

			//Group1
			subscribedGroup = new GroupChannel(this,tracker);
			subscribedGroup.start();

			startRMI(remoteObjName);
			
			Logs.MyAddress(mySubscription);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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

	public MessageRecord getMessageRecord() {
		return msgRecord;
	}
	
    public void addRestoreInitiator(String fileId, RestoreInitiator restore) {
    	restoreInitiators.put(fileId, restore);
    }
    
    public RestoreInitiator getRestoreInitiator(String fileId) {
    	return restoreInitiators.get(fileId);
    }
    
    public void removerestoreInitiator(String fileId) {
    	restoreInitiators.remove(fileId);
    }
}
