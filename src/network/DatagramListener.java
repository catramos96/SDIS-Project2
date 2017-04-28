package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import message.MessageHandler;
import peer.Peer;
import resources.Util;

public class DatagramListener extends Thread{
	
	private DatagramSocket socket = null;
	private boolean running = false;
	private Peer peer = null;
	
	public DatagramListener(Peer peer,int port){
		this.peer = peer;
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
			byte[] messageReceived = receive().getData();
			new MessageHandler(messageReceived,peer).start();
			
		}
		
		//close connection
		socket.close();
	}	
	
}
