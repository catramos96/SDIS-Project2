package resources;

import message.ActivityMessage;
import message.TopologyMessage;
import network.Subscriber;
import resources.Util.TopologyMessageType;

public class Logs {

	public static void MyAddress(Subscriber s){
		System.out.println("ME:      " + s.getSubscriberInfo());
	}
	
	//Handling Topology Messages
	
	public static void receivedTopologyMessage(TopologyMessage message){
		/*String print = new String("RECEIVED - TOPOLOGY:" + message.getType().toString() + ":" + 
						message.getSubscriber1().getSubscriberInfo());
		if(message.getSubscriber2() != null)
			print += message.getSubscriber2().getSubscriberInfo();
		
		System.out.println(print);*/
	}
	
	public static void sentTopologyMessage(TopologyMessage message){
		/*String print = new String("SENT     - TOPOLOGY:" + message.getType().toString() + ": <" + 
						message.getSubscriber1().getAddress().getHostAddress() + ":" + message.getSubscriber1().getPort() + ">");
		if(message.getSubscriber2() != null)
			print += " <" + message.getSubscriber2().getAddress().getHostAddress() + ":" + message.getSubscriber2().getPort() + ">";
		
		System.out.println(print);*/
	}
	//Topology Messages
	
	public static void notTopologyMessage(){
		System.out.println("ERROR    - TOPOLOGY: Message is not of type Topology!");
	}
	public static void notActivityMessage(){
		System.out.println("ERROR    - ACTIVITY: Message is not of type Activity!");
	}
	
	public static void newTopology(String newT, Subscriber s){
		System.out.println("NEW      - " + newT + ":" + s.getSubscriberInfo());
	}
	
	public static void remTopology(String remT,Subscriber s){
		System.out.println("REMOVED  - " + remT + ":" + s.getSubscriberInfo());
	}
	
	public static void activityMessage(ActivityMessage msg, Subscriber sender){
		String print = new String("RECEIVED - ACTIVITY: " + msg.getType().toString());
		
		if(msg.getType().compareTo(Util.ActivityMessageType.ACTIVITY) != 0)
			print += ":" + sender.getSubscriberInfo();
		
		System.out.println(print);
	}
}
