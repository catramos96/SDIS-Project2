package peer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import message.TopologyMessage;
import network.DatagramListener;
import network.GroupChannel;
import network.Subscriber;
import resources.Logs;
import resources.Util;

public class Peer {
	/*informations*/
	private int ID = 0;
	private DatagramListener comunicationChannel = null;
	private GroupChannel subscribedGroup = null;
	private Subscriber mySubscription = null;
	
	public Peer(String[] rootInfo,int myport){
		
		try {
			
			mySubscription = new Subscriber(InetAddress.getLocalHost().getHostAddress(), myport);
			Logs.MyAddress(mySubscription);
			
			InetAddress address;
			int port;
			
			//root
			if(rootInfo[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(rootInfo[0]);

			port = Integer.parseInt(rootInfo[1]);
			
			//Group1
			subscribedGroup = new GroupChannel(this);
			subscribedGroup.start();
		
			//Try to communicate with root
			TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.WHOISROOT);
			subscribedGroup.sendPrivateMessage(msg, new Subscriber(address,port));
			
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
}
