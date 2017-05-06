package resources;

import network.Subscriber;

public class Logs {

	public static void MyAddress(Subscriber s){
		System.out.println("ME: <" + s.getAddress().getHostAddress() + " " + s.getPort() + ">");
	}
	
	//Handling Topology Messages
	
	public static void receivedROOTmsg(Subscriber s){
		System.out.println("TOPOLOGY - ROOT: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void receivedPARENTmsg(Subscriber s){
		System.out.println("TOPOLOGY - PARENT: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void receivedNEWSUBSCRIBERmsg(Subscriber s){
		System.out.println("TOPOLOGY - NEWSUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void receivedSUBSCRIBERmsg(Subscriber s){
		System.out.println("TOPOLOGY - SUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void receivedWHOISROOTmsg(){
		System.out.println("TOPOLOGY - WHOISROOT");
	}
	
	public static void receivedREMSUBSCRIBERmsg(Subscriber s){
		System.out.println("TOPOLOGY - REMSUBSCRIBER: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	//Topology Messages
	
	public static void notTopologyMessage(){
		System.out.println("ERROR - TOPOLOGY: Message is not of type Topology!");
	}
	public static void notActivityMessage(){
		System.out.println("ERROR - ACTIVITY: Message is not of type Activity!");
	}
	public static void newRoot(Subscriber s){
		System.out.println("NEW - ROOT: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
}
