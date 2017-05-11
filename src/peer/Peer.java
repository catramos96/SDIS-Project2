package peer;

import filesystem.Database;
import filesystem.FileManager;
import message.MessageRMI;
import network.DatagramListener;
import network.GroupChannel;
import network.Subscriber;
import protocols.BackupInitiator;
import resources.Logs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements MessageRMI {
    /*informations*/
    private int ID;
    private FileManager fileManager;
    private Database database;

    private DatagramListener comunicationChannel = null;
    private GroupChannel subscribedGroup = null;
    private Subscriber mySubscription = null;

    public Peer(int peer_id, String[] trackerInfo, String remoteObjName){
        this.ID = peer_id;
        this.setFileManager(new FileManager(getID()));
        this.database = new Database();

        try {
            mySubscription = new Subscriber(InetAddress.getLocalHost().getHostAddress(), this.ID);
            Logs.MyAddress(mySubscription);

            //tracker
            InetAddress address = InetAddress.getByName(trackerInfo[0]);
            int port = Integer.parseInt(trackerInfo[1]);

            //Group1
            subscribedGroup = new GroupChannel(this,new Subscriber(address,port));
            subscribedGroup.start();

            startRMI(remoteObjName);

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
}
