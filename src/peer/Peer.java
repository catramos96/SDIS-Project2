package peer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import message.TopologyMessage;
import network.DatagramListener;
import network.GroupChannel;
import resources.Util;

public class Peer {
	/*informations*/
	private int ID = 0;
	private DatagramListener comunicationChannel = null;
	private GroupChannel subscribedGroup = null;
	
	public Peer(String[] rootInfo,int myport){
		
		try {
			InetAddress address;
			int port;
			
			if(rootInfo[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(rootInfo[0]);

			port = Integer.parseInt(rootInfo[1]);
			
			//comunication
			comunicationChannel = new DatagramListener(this,myport);
			comunicationChannel.start();
			
			//comunication topology
			subscribedGroup = new GroupChannel(address, port);
		
			
			//teste
			TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.ROOT,address.toString(),port);
			subscribedGroup.sendMessageToRoot(comunicationChannel, msg.buildMessage());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public GroupChannel getSubscribedGroup(){
		return subscribedGroup;
	}
}
