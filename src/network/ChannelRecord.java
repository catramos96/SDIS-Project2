package network;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class ChannelRecord used to record messages received by the communication channels.
 * Some messages records are automatic, others have to be initiated by the functions 
 * startRecordingXXX(...).
 *
 * @attribute chunkMessages - Record of the messages of type CHUNK/GOTCHUNKENH as a HashMap<String, ArrayList<Integer>>
 * where the hash represents the file identification and the ArrayList the list of chunks.
 */
public class ChannelRecord {

    //(" FileID" , <chunkNo> )
	private HashMap<String, ArrayList<Integer>> chunkMessages = null;

	/**
	 * Constructor of the ChannelRecord.
	 * Initializes all the attributes with default values.
	 */
	public ChannelRecord(){
		chunkMessages = new HashMap<>();
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
	 * Function that checks if it has a record in the chunk messages for
	 * that fileNo and chunkNo.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @return True if it has, False otherwise
	 */
	public boolean receivedChunkMessage(String fileNo, int chunkNo){

	    if(chunkMessages == null)
	        return false;

		if(chunkMessages.containsKey(fileNo))
			if(chunkMessages.get(fileNo).contains(chunkNo))
				return true;
		return false;
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
	 * Function that resets the chunk record with the fileId entry.
	 * @param fileId - File identification
	 */
	public void resetChunkMessages(String fileId) {

	    if(chunkMessages == null)
	        return;
	    if(chunkMessages.containsKey(fileId))
		    chunkMessages.remove(fileId);
	}
}