package protocols;

import filesystem.Database;
import message.ProtocolMessage;
import message.TopologyMessage;
import network.GroupChannel;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class ChunkBackupProtocol used to backup a chunk of a file until 5 tries.
 */
public class ChunkBackupProtocol extends Thread
{
    private final ProtocolMessage   msg;
    private final GroupChannel      channel;
    private final Database		    db;


	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	/**
	 * Constructor of ChunkBackupProtocol
	 */
	public ChunkBackupProtocol(Database database, GroupChannel channel, ProtocolMessage msg)
    {
        this.channel = channel;
		this.msg = msg;
		this.db = database;
        //filesystems = new ArrayList<>();
	}

	@Override
	public void run() 
	{
        int rep = 0;			//tries
		int waitingTime = Util.WAITING_TIME;

        System.out.println("Start Chunk Backup Protocol for chunk no "+msg.getChunkNo());

		//try 5 times 
		while(rep < Util.MAX_TRIES)	
		{
			System.out.println("Try number : "+rep+" to backup chunk "+msg.getChunkNo());

            //send message
            channel.sendMessageToSubscribers(msg,Util.ChannelType.MC);
			Logs.sentMessageLog(msg);

            //waits
			try {
                System.out.println(waitingTime);
                Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//replication degree achieved
			if(db.getActualRepDeg(msg.getChunkNo()+msg.getFileId()) >= msg.getReplicationDeg())
			{
				System.out.println(" - replication degree achieved - ");
				return;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;


			//request peers to tracker
			TopologyMessage msgToTracker = new TopologyMessage(Util.TopologyMessageType.GETONLINE,msg.getReplicationDeg()*2);
			channel.sendMessageToTracker(msgToTracker);
		}
	}
}
