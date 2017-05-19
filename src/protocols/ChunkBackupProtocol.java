package protocols;

import message.ProtocolMessage;
import network.GroupChannel;
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
    private ArrayList<Integer>      filesystems;


	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	/**
	 * Constructor of ChunkBackupProtocol
	 */
	public ChunkBackupProtocol(GroupChannel channel, ProtocolMessage msg)
    {
        this.channel = channel;
		this.msg = msg;
        filesystems = new ArrayList<>();
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
			System.out.println("Try number : "+rep);

            //send message
            channel.sendMessageToRoot(msg,Util.ChannelType.MC);
            System.out.println("putchunk sent");

            //waits
			try {
                System.out.println(waitingTime);
                Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//replication degree achieved
			if(getActualRepDeg() >= msg.getReplicationDeg())
			{
				System.out.println("All chunks restored");
				return;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}

		//TODO confirm
        //em caso de erro remove as mensagens guardadas ???
        //msgRecord.removeStoredMessages(fileNo, chunkNo);
	}

    public void updateStores(int senderId)
    {
        if(!filesystems.contains(senderId))
            filesystems.add(senderId);
    }

    public int getActualRepDeg() {
        return filesystems.size();
    }
}
