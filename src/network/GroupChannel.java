package network;
import java.net.InetAddress;
import java.util.ArrayList;

public class GroupChannel extends Thread{
	
	private Subscriber root = null;
	private Subscriber parent = null;
	private ArrayList<Subscriber> nextSubscribers = new ArrayList<Subscriber>();

	public GroupChannel(InetAddress rootAddress, int rootPort){
		root = new Subscriber(rootAddress,rootPort);
	}
	
	/*
	 * Information Flow Functions
	 */
	
	public void sendMessageToSubscribers(DatagramListener channel, byte[] message){
		for(Subscriber subscriber : nextSubscribers){
			channel.send(message, subscriber.getAddress(), subscriber.getPort());
		}
	}
	
	public void sendMessageToRoot(DatagramListener channel,byte[] message){
		channel.send(message, root.getAddress(), root.getPort());
	}
	
	public void sendMessageToParent(DatagramListener channel,byte[] message){
		if(parent != null)
			channel.send(message, parent.getAddress(), parent.getPort());
	}
	
	/*
	 * Topology Functions
	 */
	
	public void addSubscriber(InetAddress address, int port){
		if(hasSubscriber(address,port) == null)
			nextSubscribers.add(new Subscriber(address,port));
	}
	
	public void removeSubscriber(InetAddress address, int port){
		Subscriber p;
		if((p = hasSubscriber(address,port)) != null)
			nextSubscribers.remove(p);
	}
	
	public Subscriber hasSubscriber(InetAddress address, int port){
		for(Subscriber subscriber : nextSubscribers){
			if(subscriber.getAddress() == address && subscriber.getPort() == port)
				return subscriber;
		}
		return null;
	}
	
	public void setParent(InetAddress address, int port){
		if(parent != null){
			parent.setAddress(address);
			parent.setPort(port);
		}
		else{
			parent = new Subscriber(address, port);
		}
	}
	
	public void setRoot(InetAddress address, int port){
		root.setAddress(address);
		root.setPort(port);
	}
}
