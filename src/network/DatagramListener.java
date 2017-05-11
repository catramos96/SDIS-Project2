package network;

import peer.MessagePeerHandler;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Util;
import tracker.MessageTrackerHandler;
import tracker.Tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class DatagramListener extends Thread{
	
	private DatagramSocket socket = null;
	private boolean running = false;
	private Peer peer = null;
	private Tracker tracker = null;
	private GroupChannel subscribers = null;

    private HashMap<String, ChunkBackupProtocol> backupInitiators;
	
	public DatagramListener(Peer peer,GroupChannel channel){
		this.peer = peer;
        this.backupInitiators = new HashMap<String, ChunkBackupProtocol>();
		this.subscribers = channel;
		try {
			socket = new DatagramSocket(peer.getMySubscriptionInfo().getPort());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public DatagramListener(Tracker tracker, int port){
		this.tracker = tracker;
        this.backupInitiators = new HashMap<String, ChunkBackupProtocol>();
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
				new MessagePeerHandler(packet.getData(),new Subscriber(packet.getAddress(),packet.getPort()),peer,subscribers,backupInitiators).start();
			else if(tracker != null)
				new MessageTrackerHandler(packet.getData(),new Subscriber(packet.getAddress(),packet.getPort()),tracker).start();
		}
		
		//close connection
		socket.close();
	}

    public void addBackupInitiator(String chunkKey, ChunkBackupProtocol backup) {
        backupInitiators.put(chunkKey, backup);
    }

    public void removeBackupInitiator(String chunkKey){
        backupInitiators.remove(chunkKey);
    }
	
}
