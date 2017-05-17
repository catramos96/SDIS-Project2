package peer;

import filesystem.ChunkInfo;
import message.ActivityMessage;
import message.ProtocolMessage;
import message.TopologyMessage;
import network.GroupChannel;
import network.Subscriber;
import protocols.ChunkBackupProtocol;
import protocols.RestoreInitiator;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagePeerHandler extends Thread{

	private Peer peer = null;
	private Subscriber sender = null;
	private GroupChannel channel = null;
	private HashMap<String, ChunkBackupProtocol> chunkProts;

	public MessagePeerHandler(byte[] message, Subscriber sender, Peer peer, GroupChannel channel, HashMap<String, ChunkBackupProtocol> chunkProts){

		this.sender = sender;
		this.peer = peer;
		this.channel = channel;
		this.chunkProts = chunkProts;

		String content = new String(message);

		int firstSpace = content.indexOf(new String(" "));
		String type = content.substring(0,firstSpace);


		if(Util.isTopologyMessageType(type)){
			TopologyMessage msg = TopologyMessage.parseMessage(message);
			handleTopologyMessage(msg);
		}
		else if(Util.isProtocolMessageType(type)){
			ProtocolMessage msg = ProtocolMessage.parseMessage(message);
			handleProtocolMessage(msg);
			//TODO receber o 'backupInitiators' e fazer handle disso no store
		}
		else if(Util.isActivityMessageType(type)){
			ActivityMessage msg = ActivityMessage.parseMessage(message);
			handleActivityMessage(msg);
		}
		else{
			System.out.println(content);
		}

	}

	public void handleTopologyMessage(TopologyMessage msg){

		Logs.receivedTopologyMessage(msg);

		switch (msg.getType()) {
		//I'm the root
		case ROOT:{
			boolean sameRoot = channel.getMySubscription().equals(msg.getSubscriber());

			if(!sameRoot){
				channel.setRoot(msg.getSubscriber());
				channel.setParent(msg.getSubscriber());
				Logs.newTopology("ROOT", msg.getSubscriber());
				channel.sendMessageToSubscribers(msg,Util.ChannelType.TOP);
				Logs.sentTopologyMessage(msg);
			}

			break;
		}
		//Your parent
		case PARENT:{
			TopologyMessage warnMessage;

			//If the root  has a parent -> parent is the new root
			if(channel.iAmRoot()){
				warnMessage = new TopologyMessage(Util.TopologyMessageType.ROOT,msg.getSubscriber());
				channel.sendMessageToRoot(warnMessage,Util.ChannelType.TOP);
				Logs.sentTopologyMessage(warnMessage);

				channel.setRoot(msg.getSubscriber());
				Logs.newTopology("ROOT", msg.getSubscriber());
			}
			//If it already had a parent -> update new parent and warn the old parent to remove me from his childs
			else if(channel.hasParent() && !channel.getParent().equals(msg.getSubscriber())){
				warnMessage = new TopologyMessage(Util.TopologyMessageType.REMSUBSCRIBER,peer.getMySubscriptionInfo());
				channel.sendMessageToParent(warnMessage,Util.ChannelType.TOP);
				Logs.sentTopologyMessage(warnMessage);
			}

			//update parent
			channel.setParent(msg.getSubscriber());
			Logs.newTopology("PARENT",msg.getSubscriber());

			//warn that i'm his subscriber
			warnMessage = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBER,peer.getMySubscriptionInfo());
			channel.sendMessageToParent(warnMessage,Util.ChannelType.TOP);
			Logs.sentTopologyMessage(warnMessage);

			break;
		}
		//I'm your subscriber
		case SUBSCRIBER:{
			if(channel.addSubscriber(msg.getSubscriber()))
				Logs.newTopology("SUBSCRIBER", msg.getSubscriber());
			break;
		}
		//Remove me
		case REMSUBSCRIBER:{
			channel.removeSubscriber(msg.getSubscriber());
			Logs.remTopology("SUBSCRIBER", msg.getSubscriber());
			break;
		}
		default:{
			System.out.println("RECEIVED UNKNOWN MSG");
			break;
		}
		}

	}

	public void handleProtocolMessage(ProtocolMessage msg)
	{
		channel.sendMessageToSubscribers(msg,Util.ChannelType.MC);

		//Only processes messages sent by others
		if((peer.getID() != msg.getSenderId()) )
		{
			System.out.println("Received protocol message");

			switch (msg.getType()) {

			case PUTCHUNK:
				peer.getMessageRecord().addPutchunkMessage(msg.getFileId(), msg.getChunkNo());
				handlePutchunk(msg.getFileId(),msg.getChunkNo(),msg.getReplicationDeg(),msg.getBody());
				break;

			case STORED:
				peer.getMessageRecord().addStoredMessage(msg.getFileId(), msg.getChunkNo(), msg.getSenderId());
				handleStore(msg.getFileId(), msg.getChunkNo(),msg.getSenderId());
				break;

			case GETCHUNK:
				handleGetchunk(msg.getFileId(), msg.getChunkNo());
				break;

			case CHUNK:
				peer.getMessageRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
				handleChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
				break;

			case DELETE:
				handleDelete(msg.getFileId());
				break;

			case REMOVED:
				handleRemoved(msg.getFileId(),msg.getChunkNo(),msg.getSenderId());
				break;

			default:
				break;

			}
		}

	}

	public void handleActivityMessage(ActivityMessage msg){

		Logs.activityMessage(msg, sender);

		if(msg.getType().compareTo(Util.ActivityMessageType.ACTIVITY) == 0){
			ActivityMessage message = new ActivityMessage(Util.ActivityMessageType.ONLINE,channel.getMySubscription());
			channel.sendMessageToTracker(message);
		}

	}

	/**
	 * Peer response to other peer PUTCHUNK message.
	 * The peer will store the chunk if it has space on its disk and if it doesn't have the chunk already stored.
	 *
	 * Enhancement: If the conditions are pleased for the chunk to be stored, the peer will gather all the peers
	 * that had stored the same chunk (previously and after receiving the message) and will check if the
	 * number of peers (replication of the chunk) is bellow the desired. If it is, the peer will store the chunk,
	 * otherwise, it will not be stored, ensuring the desired replication degree of that chunk and preventing space
	 * occupation.
	 *
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param repDeg - Chunk desirable chunk replication degree
	 * @param body - Chunk content
	 */
	private synchronized void handlePutchunk(String fileId, int chunkNo, int repDeg, byte[] body)
	{
		//Owner of the file with file id
		if(peer.getDatabase().sentFileId(fileId))
			return;

		ChunkInfo c = new ChunkInfo(fileId, chunkNo, body);

		//create response message : STORED
		ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.STORED,peer.getID(),c.getFileId(),c.getChunkNo());

		//verifies chunk existence in this peer
		boolean alreadyExists = peer.getDatabase().chunkOnDB(chunkNo+fileId);

		/*
		 * If the peer doesn't have available space, it will try to free some
		 * by releasing chunks with the replication degree above average
		 */
		if(!peer.getFileManager().hasSpaceAvailable(c) && !alreadyExists)
			evictChunks();

		//verifies again (after evicting chunks) if has space available
		if(peer.getFileManager().hasSpaceAvailable(c))
		{
			/*
			 * If the peer already stored the chunk, it will warn immediately the group channel.
			 * By doing this, another peer that is pondering on storing the chunk,
			 * can be updated much faster about the actual replication of the chunk.
			 */
			if(alreadyExists)
			{
				channel.sendMessageToRoot(msg,Util.ChannelType.MDB);
				System.out.println("store sent");
			}
			else
			{
				//Waits a random time
				Util.randomDelay();

				//send STORED message
				channel.sendMessageToRoot(msg,Util.ChannelType.MDB);
				System.out.println("store sent");

				//save chunk in memory
				peer.getFileManager().saveChunk(c);

				//Save chunk info on database
				peer.getDatabase().saveChunkInfo(chunkNo+fileId,c);
			}
		}
	}

	/**
	 * The peer will try to free some space by evicting chunks whose actual replication degree is higher
	 * than the desired replication degree
	 */
	private void evictChunks()
	{
		//find chunks whose actual replication degree is higher than the desired replication
		ArrayList<ChunkInfo> chunks = peer.getDatabase().getChunksHigherReplication();

		for (int i = 0; i < chunks.size(); i++) {

			//Send message to the multicast to warn the other peers so they can update their replication degree of the chunk
			ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.REMOVED,peer.getID(),chunks.get(i).getFileId(),chunks.get(i).getChunkNo());

			channel.sendMessageToRoot(msg,Util.ChannelType.MC);
			System.out.println("removed sent");

			//Deletes the chunk from the peers disk
			String filename = chunks.get(i).getChunkNo() + chunks.get(i).getFileId();
			peer.getFileManager().deleteFile(filename);
		}
	}

	/**
	 * Peer response to other peer STORE message.
	 * The peer will record the peers that stored the chunks of the files that it backup.
	 * The peer will update the peers that stored the chunks that he also stored.
	 *
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param senderId - Sender peer identification number
	 */
	private synchronized void handleStore(String fileId, int chunkNo, int senderId){

		//Updates the Replication Degree if the peer has the chunk
		String chunkKey = chunkNo+fileId;
		if(chunkProts.containsKey(chunkKey))
			chunkProts.get(chunkKey).increaseReplicationDegree();

		if (peer.getDatabase().chunkOnDB(chunkKey))
			peer.getDatabase().updateReplicationDegree(1, chunkKey);
	}

	/**
	 * Peer response to other peer GETCHUNK message.
	 * If the peer has stored the chunkNo of the fileId, it will send the chunk content
	 * to the multicast (without enhancement) or to a private channel with the owner of the file (with enchancement)
	 * in a message of type CHUNK.
	 *
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 */
	private synchronized void handleGetchunk(String fileId, int chunkNo) {
		byte[] chunk = peer.getFileManager().getChunkContent(fileId, chunkNo);

		// Checks whether peer has chunk stored or not
		if (chunk != null) {
			ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.CHUNK, peer.getID(), fileId, chunkNo, chunk);

			// Waits random time
			Util.randomDelay();

			//If meanwhile the chunk content wasn't sent by another peer
			if(!peer.getMessageRecord().receivedChunkMessage(fileId, chunkNo)) {
				channel.sendMessageToRoot(msg,Util.ChannelType.MDR);
			}
		}
	}

	/**
	 * Peer response to other peer CHUNK message.
	 * If the peer is able to restore the file identified by fileId it will record the chunks content
	 * on Record of the peer.
	 *
	 * Enhancement: It will send a message of type GOTCHUNKENH to the multicast to warn all
	 * peers that it has received the chunks content that he asked for previously. This is the
	 * only way, for the peers, to know that the chunk was already sent, because with the restore
	 * enhancement, the chunk is sent by a private channel between the peer who sent the GETCHUNKENH
	 * and the peer who sends the CHUNK message.
	 *
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param body - Chunks content
	 */
	private synchronized void handleChunk(String fileId, int chunkNo, byte[] body){
		RestoreInitiator restoreInitiator = peer.getRestoreInitiator(fileId);
		
		// stores chunk if peer has initiated the restore protocol
		if (restoreInitiator != null) {
			restoreInitiator.addChunk(chunkNo, body);
		}
	}


	/**
	 * Peer response to other peer DELETE message.
	 * Deletes all the chunks stored for the file with the
	 * fileId identification.
	 *
	 * @param fileId - File identification
	 */
	private synchronized void handleDelete(String fileId){
		/*
        //verifies if the current peer has chunks stored from this file
        if(peer.getRecord().myChunksBelongsToFile(fileId))
        {
            //deletes chunks from disk
            peer.getFileManager().deleteChunks(fileId);
            //remove from record
            peer.getRecord().deleteMyChunksByFile(fileId);
        }
		 */
	}

	/**
	 * Peer response to other peer REMOVED message.
	 * If the peer has stored/backup this chunkNo of the fileId,
	 * it will update it's replication degree and peers.
	 * If the actual replication degree drops bellow the desired, a chunk
	 * backup protocol is initiated after a random time and if it didn't
	 * received a PUTCHUNK message for the same fileId and chunkNo meanwhile.
	 *
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peerNo - Peer who removed the chunk from his disk
	 */
	private synchronized void handleRemoved(String fileId, int chunkNo, int peerNo){
		/*
        Record record = peer.getRecord();
        FileInfo info = record.getBackupFileInfoById(fileId);

        byte[] data = null;
        int repDegree = 0;
        int desiredRepDegree = 0;
        boolean hasChunk = false;

        //This peer initiated the backup of this file (with fileId received)
        if(record.checkStoredChunk(fileId, chunkNo) != null && info != null)
        {
            //Update stored record
            record.deleteStored(fileId, chunkNo, peerNo);
            desiredRepDegree = info.getNumChunks();

            //Actual replication degree
            ArrayList<Integer> peersWithChunk = record.checkStoredChunk(fileId, chunkNo);
            if(peersWithChunk != null)
                repDegree = peersWithChunk.size();

            if(repDegree < desiredRepDegree){
                //Get data of the chunk
                ArrayList<Chunk> chunks = peer.getFileManager().splitFileInChunks(info.getPath());
                Chunk c = chunks.get(chunkNo);
                data = c.getData();
            }
        }
        //Not Owner but has the chunk stored
        else if(peer.getRecord().checkMyChunk(fileId, chunkNo))
        {
            //remove peer from 'Record'
            peer.getRecord().remPeerWithMyChunk(fileId, chunkNo, peerNo);

            //get data of the chunk
            data = peer.getFileManager().getChunkContent(fileId, chunkNo);
            repDegree = peer.getRecord().getMyChunk(fileId, chunkNo).getAtualRepDeg();
            desiredRepDegree = peer.getRecord().getMyChunk(fileId, chunkNo).getReplicationDeg();

            hasChunk = true;
        }


		 * If replicaiton degree is bellow desired it will start the chunkbackup protocol
		 * only if after a random time it doesn't received any putchunk for the same fileId and chunkNo

        if(repDegree < desiredRepDegree){
            peer.getMessageRecord().removePutChunkMessages(fileId, chunkNo);	//reset recording
            peer.getMessageRecord().startRecordingPutchunks(fileId);	//start record

            Util.randomDelay();

            if(!peer.getMessageRecord().receivedPutchunkMessage(fileId, chunkNo)){

                //sends PUTCHUNK message
                Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),fileId,chunkNo,repDegree,data);
                //Logs.sentMessageLog(msg);
                new ChunkBackupProtocol(peer.getMdb(), peer.getMessageRecord(), msg).start();


                if(hasChunk){
                    Util.randomDelay();

                    peer.getMessageRecord().addStoredMessage(fileId, chunkNo, peer.getID());
                    //Warns the peers that it also has the chunk
                    msg = new Message(MessageType.STORED,peer.getVersion(),peer.getID(),fileId,chunkNo);
                    peer.getMc().send(msg);
                }
            }
        }
		 */
	}
}
