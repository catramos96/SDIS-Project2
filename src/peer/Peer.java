package peer;

import message.MessageRMI;
import network.DatagramListener;
import network.GroupChannel;
import network.Subscriber;
import resources.Logs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements MessageRMI {
	/*informations*/
	private int ID = 0;
	private DatagramListener comunicationChannel = null;
	private GroupChannel subscribedGroup = null;
	private Subscriber mySubscription = null;
	
	public Peer(String[] trackerInfo,int myport,String remoteObjName){
		
		try {
			
			mySubscription = new Subscriber(InetAddress.getLocalHost().getHostAddress(), myport);
			Logs.MyAddress(mySubscription);
			
			//tracker
			InetAddress address = InetAddress.getByName(trackerInfo[0]);
			int port = Integer.parseInt(trackerInfo[1]);
			
			//Group1
			subscribedGroup = new GroupChannel(this,new Subscriber(address,port));
			subscribedGroup.start();

            startRMI(remoteObjName);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
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
}
