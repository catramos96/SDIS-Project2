package resources;

import network.Subscriber;

public class Logs {

	public static void MyAddress(Subscriber s){
		System.out.println("ME: <" + s.getAddress().getHostAddress() + " " + s.getPort() + ">");
	}
	
	//Handling Topology Messages
	
	public static void newRoot(Subscriber s){
		System.out.println("TOPOLOGY - ROOT: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void newParent(Subscriber s){
		System.out.println("TOPOLOGY - PARENT: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void newSubscriber(Subscriber s){
		System.out.println("TOPOLOGY - NEWSUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void yourSubscriber(Subscriber s){
		System.out.println("TOPOLOGY - SUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void whoIsRootMessage(){
		System.out.println("TOPOLOGY - WHOISROOT");
	}
	
	public static void removeSubscriber(Subscriber s){
		System.out.println("TOPOLOGY - REMSUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	//Topology Messages
	
	public static void notTopologyMessage(){
		System.out.println("Message is not of type Topology!");
	}
	public static void notActivityMessage(){
		System.out.println("Message is not of type Activity!");
	}
}
