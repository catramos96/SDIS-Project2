package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import peer.MessagePeerHandler;
import peer.Peer;
import resources.Util;
import tracker.MessageTrackerHandler;
import tracker.Tracker;

public class DatagramListener extends Thread{
	
	private DatagramSocket socket = null;
	private boolean running = false;
	private Peer peer = null;
	private Tracker tracker = null;
	private GroupChannel subscribers = null;
	
	public DatagramListener(Peer peer,GroupChannel channel){
		this.peer = peer;
		this.subscribers = channel;
		try {
			socket = new DatagramSocket(peer.getMySubscriptionInfo().getPort());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public DatagramListener(Tracker tracker, int port){
		this.tracker = tracker;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void send(byte[] message,InetAddress address, int port)
	{
		DatagramPacket packet;
		packet = new DatagramPacket(message, message.length, address, port);
		
		try {
			socket.send(packet);
		} 
		catch (IOException e) 	{
			e.printStackTrace();
		}
	}
	
	public DatagramPacket receive()
	{
		byte[] m_buf = new byte[Util.PACKET_MAX_SIZE];
		DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
		try {
			socket.receive(packet);
		} 
		catch (IOException e) 	{
			e.printStackTrace();
		}
		
		return packet;
	}
	
	public void run(){
		
		running = true;
	
		while(running)
		{
			DatagramPacket packet = receive();
			if(peer != null)
				new MessagePeerHandler(packet.getData(),new Subscriber(packet.getAddress(),packet.getPort()),peer,subscribers).start();
			else if(tracker != null)
				new MessageTrackerHandler(packet.getData(),new Subscriber(packet.getAddress(),packet.getPort()),tracker).start();
		}
		
		//close connection
		socket.close();
	}	
	
}
