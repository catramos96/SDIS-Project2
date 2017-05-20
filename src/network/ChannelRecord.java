package network;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class ChannelRecord used to record messages received by the communication channels.
 * Some messages records are automatic, others have to be initiated by the functions 
 * startRecordingXXX(...).
 * 
 * @attribute storedMessages - Record of messages of type STORED as a HashMap<String, HashMap<Integer, ArrayList<Integer>>>
 * where the hash String represents a file identification and the HashMap<Integer, ArrayList<Integer>> represents all the chunks
 * associated with it. In the inner HashMap, the hash Integer represents the chunk identification number and the ArrayList the list of peers.
 * 
 * @attribute chunkMessages - Record of the messages of type CHUNK/GOTCHUNKENH as a HashMap<String, ArrayList<Integer>>
 * where the hash represents the file identification and the ArrayList the list of chunks.
 * 
 * @attribute putchunkMessages - Record of the messages of type PUTCHUNK as a HashMap<String, ArrayList<Integer>>
 * where the hash represents the file identification and the ArrayList the list of chunks.
 * 
 * @attribute initiatorMessages - Record of the messages of type INITIATOR as a HashMap<String, Integer>
 * where the hash represents the file identification and the key the chunk identification number.
 */
public class ChannelRecord {

    //( "chunkKey" , <peerID> )
	private HashMap<String, ArrayList<Integer>> storedMessages = null;
    //(" FileID" , <chunkNo> )
	private HashMap<String, ArrayList<Integer>> chunkMessages = null;

	//private HashMap<String, ArrayList<Integer>> putchunkMessages = null;
	//private HashMap<String, Integer> initiatorMessages = null;

	/**
	 * Constructor of the ChannelRecord.
	 * Initializes all the attributes with default values.
	 */
	public ChannelRecord(){
		storedMessages = new HashMap<>();
		chunkMessages = new HashMap<>();

		//putchunkMessages = new HashMap<String, ArrayList<Integer>>();
		//initiatorMessages = new HashMap<String, Integer>();
	}

	/*
	 * Start Recording
	 */
		
	/**
	 * Function that starts recording the putchunkMessages of a certain file.
	 * @param fileNo - File identification
	 *
	public void startRecordingPutchunks(String fileNo){
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		if(!putchunkMessages.containsKey(fileNo))
			putchunkMessages.put(fileNo, tmp);
	}*/
	
	/**
	 * Function that start recording the initiatorMessages of a certain file.
	 * @param fileId - File identification
	 *
	public void startRecordingInitiators(String fileId){
		initiatorMessages.put(fileId, null);
	}
	*/
	/*
	 * ADDS
	 */

	/**
	 * Function that adds a store message to the record. It doesn't add if it already exists
	 * a record for that sender peer.
	 * @param chunkKey - Chunk identification number + File identification
	 * @param sender - Sender identification
	 */
	public void addStoredMessage(String chunkKey, Integer sender){
		HashMap<Integer,ArrayList<Integer>> chunks = new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> peers = new ArrayList<Integer>();

		if(storedMessages.containsKey(chunkKey))
		{
			peers = storedMessages.get(chunkKey);
            if(peers.contains(sender))
                return;
		}

		peers.add(sender);
		storedMessages.put(chunkKey, peers);
	}

	/**
	 * Function that adds a chunk message record. It doesn't add if it already exists
	 * a record for that sender peer.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 */
	public void addChunkMessage(String fileNo, int chunkNo){
		ArrayList<Integer> chunks = new ArrayList<Integer>();
		if(chunkMessages.containsKey(fileNo)){
			chunks = chunkMessages.get(fileNo);
			if(chunks.contains(chunkNo))
				return;
		}

		chunks.add(chunkNo);
		chunkMessages.put(fileNo, chunks);
	}

	/**
	 * Function that adds a putchunk message record. It will only be added if
	 * the record was started previously for this type of message with this fileNo.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification
	 *
	public void addPutchunkMessage(String fileNo, int chunkNo){

		if(putchunkMessages.containsKey(fileNo)){
			ArrayList<Integer> putchunks = putchunkMessages.get(fileNo);
			if(putchunks.contains(chunkNo))
				return;
			else
			{
				putchunks.add(chunkNo);
				putchunkMessages.put(fileNo, putchunks);
			}
		}
	}*/
	
	/**
	 * Function that adds a initiator message record. It will only be added if
	 * the record was started previously for this type of message with this fileNo.
	 * @param fileId
	 * @param senderId
	 *
	public void addInitiatorMessage(String fileId, int senderId){
		if(initiatorMessages.containsKey(fileId))
			initiatorMessages.put(fileId, senderId);
	}*/

	/*
	 * CONTAINS
	 */

	/**
	 * Function that checks if it has a record in the stored messages for
	 * that fileNo and chunkNo.
	 * @param chunkKey - Chunk identification number+File identification
	 * @return True if it has, False otherwise
	 */
	public boolean receivedStoredMessage(String chunkKey){
		return storedMessages.containsKey(chunkKey);
	}

	/**
	 * Function that checks if it has a record in the chunk messages for
	 * that fileNo and chunkNo.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @return True if it has, False otherwise
	 */
	public boolean receivedChunkMessage(String fileNo, int chunkNo){
		if(chunkMessages.containsKey(fileNo))
			if(chunkMessages.get(fileNo).contains(chunkNo))
				return true;
		return false;
	}

	/**
	 * Function that checks if it has a record in the putchunk messages for
	 * that fileNo and chunkNo.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @return True if it has, False otherwise
	 *
	public boolean receivedPutchunkMessage(String fileNo, int chunkNo){
		if(putchunkMessages.containsKey(fileNo))
			if(putchunkMessages.get(fileNo).contains(chunkNo))
				return true;
		return false;
	}*/
	
	/**
	 * Function that checks if it has a record in the initiator messages for
	 * that fileNo.
	 * @param fileNo - File identification
	 * @return True if it has, False otherwise
	 *
	public boolean receivedInitiatorMessage(String fileNo){
		if(initiatorMessages.get(fileNo) != null)
			return true;
		return false;
	}*/

	/*
	 * REMOVE RECORD
	 */
	
	/**
	 * Function that removes from the stored record, the entry with that
	 * fileNo and chunkNo.
	 * @param chunkKey - Chunk identification number+File identification
	 */
	public void removeStoredMessages(String chunkKey)
    {
		if(storedMessages.containsKey(chunkKey))
            storedMessages.remove(chunkKey);
	}

	/**
	 * Function that removes from the chunk record, the entry with that
	 * fileNo.
	 * @param fileNo - File identification
	 */
	public void removeChunkMessages(String fileNo){
		if(chunkMessages.containsKey(fileNo)){
			chunkMessages.remove(fileNo);
		}
	}

	/**
	 * Function that removes from the putchunk record, the entry with that
	 * fileNo and chunkNo.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 *
	public void removePutChunkMessages(String fileNo, int chunkNo){
		if(putchunkMessages.containsKey(fileNo))
		{
			ArrayList<Integer> putchunks = putchunkMessages.get(fileNo);
			if(putchunks.contains(chunkNo))
			{
				putchunks.remove((Integer)chunkNo);
				putchunkMessages.put(fileNo, putchunks);
			}
		}
	}*/

	/**
	 * Function that resets the chunk record with the fileId entry.
	 * @param fileId - File identification
	 */
	public void resetChunkMessages(String fileId) 
	{
		chunkMessages.remove(fileId);
	}


	public void resetStoreMessages(String fileId) {

        for (String chunkKey : storedMessages.keySet())
            if(chunkKey.contains(fileId))
                storedMessages.remove(chunkKey);
    }
	
	/**
	 * Function that resets the initiator record with the fileId entry.
	 * @param fileId - File identification
	 *
	public void resetInitiatorMessages(String fileId) 
	{
		initiatorMessages.remove(fileId);
	}
*/
	/*
	 * GETS
	 */
	/**
	 * Function that gets the size of the list of peers that are recorded in the stored messages for
	 * that fileNo and chunkNo. If there are no peers, it will return 0.
	 * @param chunkKey - Chunk identification number+File identification
	 * @return List of peers or Null in case there are any.
	 */
	public synchronized int getStoredMessagesNum(String chunkKey){

		if(receivedStoredMessage(chunkKey))
			return storedMessages.get(chunkKey).size();

		return 0;
	}
}