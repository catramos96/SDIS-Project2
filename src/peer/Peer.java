package peer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import message.TopologyMessage;
import network.DatagramListener;
import network.GroupChannel;
import network.Subscriber;
import resources.Util;

public class Peer {
	/*informations*/
	private int ID = 0;
	private DatagramListener comunicationChannel = null;
	private GroupChannel subscribedGroup = null;
	private Subscriber mySubscription = null;
	
	public Peer(String[] rootInfo,int myport){
		
		try {
			
			mySubscription = new Subscriber(InetAddress.getLocalHost(), myport);
			InetAddress address;
			int port;
			
			//root
			if(rootInfo[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(rootInfo[0]);

			port = Integer.parseInt(rootInfo[1]);
			
			//comunication topology
			subscribedGroup = new GroupChannel(address, port,this);
			subscribedGroup.start();
		
			//teste
			//dizer que eu sou a root
			TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.ROOT,mySubscription.getAddress().getHostAddress(),myport);
			subscribedGroup.sendMessageToRoot(msg);
			
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
