package resources;

import java.util.Random;

/**
 * Class Util containing important constants and others.
 */
public class Util {
	
	//PROTOCOL
	public static final int MAX_TRIES = 5;				//Max tries
	public static final int RND_DELAY = 400;			//Max random delay
	public static final int WAITING_TIME = 1000;
	public static final int MAX_AVG_DELAY_TIME = 14000; //Maximum delay time for a protocol such as backup and restore 
														//400+800+1600+3200+6400 (5 rep) + AVG1600
	public static final int TIME_REINFORCEMENT = 2;
	
	//PEER
	
	public static final int DISK_SPACE_DEFAULT = 1000000;
	
	//MESSAGES
	
	public static final char CR = 0xD;								
	public static final char LF = 0xA;
	public static final String LINE_SEPARATOR = "" + CR + LF;
	
	public static enum ProtocolMessageType
	{
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED, GETINITIATOR, INITIATOR, GOTCHUNKENH, GETCHUNKENH
	}
	
	public static boolean isProtocolMessageType(String test){
		ProtocolMessageType[] types = ProtocolMessageType.values();
		
		for(ProtocolMessageType t : types){
			if(test.compareTo(t.name()) == 0)
				return true;
		}
		return false;
	}
	
	public static enum TopologyMessageType
	{
		ONLINE, GETONLINE, SUBSCRIBERS, PUT, GET, REMOVE, DELETE, INFO, CHECK
	}
	
	public static boolean isTopologyMessageType(String test){
		TopologyMessageType[] types = TopologyMessageType.values();
		
		for(TopologyMessageType t : types){
			if(test.compareTo(t.name()) == 0)
				return true;
		}
		return false;
	}
	
	public static int PACKET_MAX_SIZE = 65000;
	
	//ChunkInfo
	public static int CHUNK_MAX_SIZE = 64000;
	
	//FileManager - Directories
	public static final String PEERS_DIR = new String("../peersDisk/");
	public static final String CHUNKS_DIR = new String("/chunks/");
	public static final String RESTORES_DIR = new String("/restores/");
	public static final String LOCAL_DIR = new String("/localFiles/");

	/**
	 * Function that waits a random delay time.
	 */
	public static void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//Topologia
	public static final int MAX_N_SUBSCRIBERS = 10;
	
	//CHANNELS
	
	public static enum ChannelType{
		TOP, MC,MDR, MDB
	}
	
}
