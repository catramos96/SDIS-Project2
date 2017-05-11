package protocols;

import message.ProtocolMessage;
import network.GroupChannel;
import resources.Util;

import java.util.Random;

/**
 * Class ChunkBackupProtocol used to backup a chunk of a file until 5 tries.
 */
public class ChunkBackupProtocol extends Thread{
    private final Random delay;
    private int atualRepDeg;
    private final ProtocolMessage msg;
    private final GroupChannel channel;

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	/**
	 * Constructor of ChunkBackupProtocol
	 */
	public ChunkBackupProtocol(GroupChannel channel, ProtocolMessage msg){
        this.channel = channel;
		this.msg = msg;
        this.delay = new Random();
		this.atualRepDeg = 0;
	}

	@Override
	public void run() 
	{
        int rep = 0;			//tries
		int waitingTime = Util.WAITING_TIME;

        System.out.println("Start Chunk Backup Protocol for chunk no "+msg.getChunkNo());

		//INIT replication degree counting
        channel.addBackupInitiator(msg.getChunkNo()+msg.getFileId(), this);
		
		//try 5 times 
		while(rep < Util.MAX_TRIES)	
		{
			System.out.println("Try number : "+rep);
			
			//TODO : if it receives a putchunk for the same file and chunk, the backup will end ?
			//if(msgRecord.receivedPutchunkMessage(fileNo, chunkNo))
			//	return;

            //TODO esta correto? (cat)
            //send message
			channel.sendMessageToParent(msg);
            System.out.println("putchunk sent");

            //waits
			try {
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//replication degree achieved
			if(this.atualRepDeg >= msg.getReplicationDeg())
			{
				System.out.println("All chunks restored");
				break;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		
		//END replication degree counting
        channel.removeBackupInitiator(msg.getChunkNo()+msg.getFileId());
	}

	public void increaseReplicationDegree(){
	    this.atualRepDeg++;
    }

    public int getAtualRepDeg()
    {
        return atualRepDeg;
    }
}
