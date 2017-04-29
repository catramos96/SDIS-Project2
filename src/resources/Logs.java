package resources;

import network.Subscriber;

public class Logs {

	
	public static void newRoot(Subscriber s){
		System.out.println("New Root: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void newParent(Subscriber s){
		System.out.println("New Parent: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
	
	public static void newSubscriber(Subscriber s){
		System.out.println("New Subscriber: <" + s.getAddress().getHostAddress() + ":" + s.getPort() + ">");
	}
}
