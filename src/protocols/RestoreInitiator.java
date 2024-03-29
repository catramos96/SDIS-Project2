package protocols;

import peer.Peer;
import resources.Logs;
import resources.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import client.Notification;
import filesystem.FileInfo;
import message.ProtocolMessage;
import message.TopologyMessage;

/**
 * Class RestoreInitiator used to initiate the restore protocol of a file.
 */
public class RestoreInitiator extends Thread
{
    private Peer        peer;
    private String      filePath;
    private byte[][]    data;

	private static final int MAX_TRIES = 5;
	private static final int NO_PEERS = 5;

    /**
     * Constructor of RestoreInitiator
     */
    public RestoreInitiator(Peer peer, String filePath) {
        this.peer = peer;
        this.filePath = filePath;
    }

    public void addChunk(int chunkNo, byte[] chunkData)
    {
    	data[chunkNo] = chunkData;
    }
    
    @Override
    public void run()
    {
        FileInfo fileInfo = peer.getDatabase().getFileInfo(filePath);
        
    	// Checks whether peer has requested the backup of the file or not
        if (fileInfo == null) {
        	String notificationMsg = "The requested file: " + filePath + " doesn't exist in the database.";
            System.out.println(notificationMsg);
            (new Thread(new Notification("Restore " + peer.getID(), notificationMsg))).start();
            return;
        }

        int numberChunks = fileInfo.getNumChunks();
        data = new byte[numberChunks][];
        String fileID = fileInfo.getFileId();

        // add initiator to peer
        peer.addRestoreInitiator(fileID, this);

        for (int i = 0; i < numberChunks; i++)
        {
        	String chunkId = fileInfo.getFileId() + i;
            		
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.GETCHUNK, peer.getID(), fileID, i, peer.getMySubscriptionInfo().getAddress().getHostAddress(), peer.getMySubscriptionInfo().getMdrPort());
            
            // Tries sending GETCHUNK message for MAX_TRIES
            for (int attempts = 0; attempts < MAX_TRIES; attempts++)
            {
            	// get peers from DHT to send GETCHUNK to
                TopologyMessage getPeersMsg = new TopologyMessage(Util.TopologyMessageType.GET, chunkId, NO_PEERS, attempts);
                
                peer.getSubscribedGroup().sendMessageToTracker(getPeersMsg);
                Logs.sentTopologyMessage(getPeersMsg);
                
            	// Sends message again if peer still hasn't received chunk
				if(data[i] == null) {
		            peer.getSubscribedGroup().sendMessageToSubscribers(msg,Util.ChannelType.MC);
					Util.randomDelay();
				} else {
					break;
				}
			}
            
            // reset subscriber list
            peer.getSubscribedGroup().resetSubscribers();
            
            // Checks if chunk has been recovered.
			if (data[i] == null) {
				System.out.println("Failed to recover " + filePath + ".");
				peer.getChannelRecord().resetChunkMessages(fileID);
				return;
			}
        }
        
        Path p = Paths.get(filePath);
		String file = p.getFileName().toString();

		try {
			peer.getFileManager().restoreFile(file, data, peer.getEncrypt());
		} catch (IOException e) {
			System.out.println("Failed to recover " + filePath + ".");
			e.printStackTrace();
			return;
		} catch (InvalidKeyException e) {
			System.out.println("Failed to recover (invalid cypher key): " + filePath + ".");
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		peer.getDatabase().addRestoredFile(filePath,fileInfo);
		
		peer.removeRestoreInitiator(fileID);

    	String notificationMsg = file + " has been restored successfully.";
        System.out.println(notificationMsg);
        (new Thread(new Notification("Restore " + peer.getID(), notificationMsg))).start();
		System.out.println(notificationMsg);
    }
}
