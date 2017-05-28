package peer;

import filesystem.ChunkInfo;
import filesystem.Database;
import message.ProtocolMessage;
import message.TopologyMessage;
import network.GroupChannel;
import network.Subscriber;
import protocols.RestoreInitiator;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;

public class MessagePeerHandler extends Thread{

    private Peer peer = null;
    private Subscriber sender = null;
    private GroupChannel channel = null;
    private Util.ChannelType fromChannelType;

    public MessagePeerHandler(Util.ChannelType channelType, byte[] message, Subscriber sender, Peer peer, GroupChannel channel) {

        this.sender = sender;
        this.peer = peer;
        this.channel = channel;
        this.fromChannelType = channelType;

        String content = new String(message);

        int firstSpace = content.indexOf(new String(" "));
        String type = content.substring(0, firstSpace);

        if (Util.isTopologyMessageType(type)) {
            TopologyMessage msg = TopologyMessage.parseMessage(message);
            handleTopologyMessage(msg);
        } else if (Util.isProtocolMessageType(type)) {
            ProtocolMessage msg = ProtocolMessage.parseMessage(message);
            handleProtocolMessage(msg);
        } else {
            System.out.println(content);
        }
    }

    public void handleTopologyMessage(TopologyMessage msg){

        Logs.receivedTopologyMessage(msg);

        switch (msg.getType()) {

            case SUBSCRIBERS:{
                channel.addSubscribers(msg.getSubscribersGroup());
                break;
            }
            case INFO:{

                ArrayList<String> keys = msg.getKeys();
                ArrayList<Integer> reps = msg.getRepDegs();

                for(int i = 0; i < keys.size();i++){
                    if(reps.get(i) == 0) {
                        //deletes from the file system
                        peer.getFileManager().deleteFile(keys.get(i));

                        //update database
                        peer.getDatabase().removeStoredChunk(keys.get(i));
                    }

                    else {
                        //update repDeg
                        peer.getDatabase().updateActualRepDeg(reps.get(i), keys.get(i));
                        //renovate expiration date
                        peer.getDatabase().renovateChunks(msg.getKeys());

                        ChunkInfo chunk = peer.getDatabase().getChunkInfo(keys.get(i));
                        byte[] data = peer.getFileManager().getChunkContent(chunk.getFileId(),chunk.getChunkNo());
                        chunk.setData(data);

                        if(peer.getDatabase().getStoredChunks().containsKey(keys.get(i))) {
                            chunk = peer.getDatabase().getStoredChunks().get(keys.get(i));

                            //Check if new replication degree is bellow the desired
                            if(chunk.getReplicationDeg() > chunk.getActualRepDeg()){
                                peer.chunkBackup(chunk);
                            }
                        }
                    }

                }

                break;
            }
            default:{
                System.out.println("RECEIVED UNKNOWN MSG");
                System.out.println("teste" + msg.toString());
                break;
            }
        }

    }

    public void handleProtocolMessage(ProtocolMessage msg)
    {

        //Only processes messages sent by others
        if((peer.getID() != msg.getSenderId()) )
        {
            Logs.receivedMessageLog(msg);

            switch (msg.getType()) {

                case PUTCHUNK:
                    handlePutchunk(msg.getFileId(),msg.getChunkNo(),msg.getReplicationDeg(),msg.getAddress(), msg.getPort(),msg.getBody()); 
                    break;

                case STORED:
                    handleStore(msg);
                    break;

                case GETCHUNK:
                    handleGetchunk(msg.getFileId(), msg.getChunkNo(), msg.getAddress(), msg.getPort());
                    break;

                case CHUNK:
                    peer.getChannelRecord().addChunkMessage(msg.getFileId(), msg.getChunkNo());
                    handleChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
                    break;

                case DELETED:
                    peer.getChannelRecord().resetChunkMessages(msg.getFileId());
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
     * @param address - Address of the backup initiator
     * @param port - Port of the backup initiator's MDB channel
     * @param body - Chunk content
     */
    private synchronized void handlePutchunk(String fileId, int chunkNo, int repDeg, String address, int port, byte[] body)
    {
        //Owner of the file with file id
        if(peer.getDatabase().hasSentFileByFileID(fileId))
            return;

        //create chunk
        ChunkInfo c = new ChunkInfo(fileId, chunkNo, body);
        c.setReplicationDeg(repDeg);
       
        
        //create response message : STORED
        ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.STORED,peer.getID(),c.getFileId(),c.getChunkNo());

        //verifies chunk existence in this peer
        boolean alreadyExists = peer.getDatabase().hasChunkStored(c.getChunkKey());

        //we dont want more than 1 message STORED per peer
        if(alreadyExists)
            return;

		/*
		 * If the peer doesn't have available space, it will try to free some
		 * by releasing chunks with the replication degree above average
		 */
        if(!peer.getFileManager().hasSpaceAvailable(c))
            evictChunks();

        //verifies again (after evicting chunks) if has space available
        if(peer.getFileManager().hasSpaceAvailable(c))
        {
            //Waits a random time
            Util.randomDelay();

            // creates temporary subscriber
            Subscriber s = new Subscriber(address, -1, -1, -1, port);

            //send STORED message
            channel.sendPrivateMessage(msg, s, Util.ChannelType.MDB);
            Logs.sentMessageLog(msg);

            //save chunk in memory
            peer.getFileManager().saveChunk(c);

            //Save chunk info on database
            peer.getDatabase().saveChunkInfo(chunkNo+fileId,c);

            // creates PUT message to update DHT
            TopologyMessage putMsg = new TopologyMessage(Util.TopologyMessageType.PUT, c.getChunkKey(), peer.getMySubscriptionInfo());

            // send PUT message
            peer.getSubscribedGroup().sendMessageToTracker(putMsg);
            Logs.sentTopologyMessage(putMsg);
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
            channel.sendMessageToSubscribers(msg,Util.ChannelType.MC);
            Logs.sentMessageLog(msg);

            //Deletes the chunk from the peers disk
            String filename = chunks.get(i).getChunkNo() + chunks.get(i).getFileId();
            peer.getFileManager().deleteFile(filename);
         
            //notify tracker
			TopologyMessage msgTracker = new TopologyMessage(Util.TopologyMessageType.REMOVE,chunks.get(i).getChunkKey(),peer.getMySubscriptionInfo());
            peer.getSubscribedGroup().sendMessageToTracker(msgTracker);
            

            //update database
            peer.getDatabase().removeStoredChunk(chunks.get(i).getChunkKey());
        }
    }

    /**
     * Peer response to other peer STORE message.
     * The peer will record the peers that stored the chunks of the files that it backup.
     * The peer will update the peers that stored the chunks that he also stored.
     *
     */
    private synchronized void handleStore(ProtocolMessage msg){

        String chunkKey = msg.getChunkNo() + msg.getFileId();

        //TODO rem
        //Updates the Replication Degree if the peer has the chunk stored
        if(peer.getDatabase().hasChunkStored(chunkKey))
        {
            //creates CHECK message to update my chunk replication degree
            ArrayList<String> checkChunks = new ArrayList<>();
            checkChunks.add(chunkKey);
            TopologyMessage checkMsg = new TopologyMessage(Util.TopologyMessageType.CHECK, checkChunks);

            // send CHECK message
            peer.getSubscribedGroup().sendMessageToTracker(checkMsg);
            Logs.sentTopologyMessage(checkMsg);
        }

        //only initiator peer receive stores
        //Record the storedChunks in case the peer is the OWNER of the backup file
        if(peer.getDatabase().hasSentChunk(chunkKey))
        {
            System.out.print("aqui");
            peer.getDatabase().addFilesystem(chunkKey);
        }

    }

    /**
     * Peer response to other peer GETCHUNK message.
     * If the peer has stored the chunkNo of the fileId, it will send the chunk content
     * to a private channel with the owner of the file
     * in a message of type CHUNK.
     *
     * @param fileId - File identification
     * @param chunkNo - Chunk identification number
     * @param address - Address of the restore initiator
     * @param port - Port of the restore initiator's MDR channel
     */
    private synchronized void handleGetchunk(String fileId, int chunkNo, String address, int port)
    {
        byte[] chunk = peer.getFileManager().getChunkContent(fileId, chunkNo);

        // Checks whether peer has chunk stored or not
        if (chunk != null)
        {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.CHUNK, peer.getID(), fileId, chunkNo, chunk);

            // Waits random time
            Util.randomDelay();

            //If meanwhile the chunk content wasn't sent by another peer
            if(!peer.getChannelRecord().receivedChunkMessage(fileId, chunkNo))
            {
                // creates temporary subscriber
                Subscriber s = new Subscriber(address, -1, -1, port, -1);

                // send CHUNK message
                channel.sendPrivateMessage(msg, s, Util.ChannelType.MDR);
                Logs.sentMessageLog(msg);
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
    private synchronized void handleChunk(String fileId, int chunkNo, byte[] body)
    {
        RestoreInitiator restoreInitiator = peer.getRestoreInitiator(fileId);
        // stores chunk if peer has initiated the restore protocol
        if (restoreInitiator != null)
        {
            restoreInitiator.addChunk(chunkNo, body);

            // builds empty CHUNK message
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.CHUNK, peer.getID(), fileId, chunkNo, new byte[0]);

            // sends empty CHUNK so they are notified the initaor has already received the chunk.
            peer.getSubscribedGroup().sendMessageToSubscribers(msg,Util.ChannelType.MC);
        }
    }

    /**
     * Peer response to other peer DELETE message.
     * Deletes all the chunks stored for the file with the
     * fileId identification.
     *
     * @param fileId - File identification
     */
    private synchronized void handleDelete(String fileId)
    {
        if(!peer.getDatabase().hasSentFileByFileID(fileId))
        {
            //deletes chunks with fileID from disk
            peer.getFileManager().deleteChunks(fileId);

            //remove chunks from database
            peer.getDatabase().removeStoredChunksFromFileID(fileId);
        }
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
     * @param senderId - Peer who removed the chunk from his disk
     */
    private synchronized void handleRemoved(String fileId, int chunkNo, int senderId)
    {
        Database database = peer.getDatabase();

        //This peer initiated the backup of this file (with fileId received)
        if(database.hasSentFileByFileID(fileId))
        {
            //down replication degree and update chunks
            boolean bellow = database.removeFilesystem(chunkNo+fileId);

            //if actual replication degree is bellow desired replication degree
            //must start chunk backup
            if(bellow) {
                ChunkInfo chunk = database.getSentChunkInfo(chunkNo+fileId);
                peer.chunkBackup(chunk);
            }

        }
        else if(database.hasChunkStored(chunkNo+fileId))
        {
            //creates CHECK message to update my chunk replication degree
            ArrayList<String> checkChunks = new ArrayList<>();
            checkChunks.add(chunkNo+fileId);
            TopologyMessage checkMsg = new TopologyMessage(Util.TopologyMessageType.CHECK, checkChunks);

            // send CHECK message
            peer.getSubscribedGroup().sendMessageToTracker(checkMsg);
            Logs.sentTopologyMessage(checkMsg);
        }

    }
}
