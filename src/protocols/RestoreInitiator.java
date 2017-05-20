package protocols;

import peer.Peer;
import resources.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import filesystem.FileInfo;
import message.Message;
import message.ProtocolMessage;

/**
 * Class RestoreInitiator used to initiate the restore protocol of a file.
 */
public class RestoreInitiator extends Thread {
    private Peer peer;
    private String filePath;

	private static final int MAX_TRIES = 5;
    
    private byte[][] data;

    /**
     * Constructor of RestoreInitiator
     */
    public RestoreInitiator(Peer peer, String filePath) {
        this.peer = peer;
        this.filePath = filePath;
    }

    public void addChunk(int chunkNo, byte[] chunkData) {
    	data[chunkNo] = chunkData;
    }
    
    @Override
    public void run() {
        FileInfo fileInfo = peer.getDatabase().getFileData(filePath);
        
    	// Checks whether peer has requested the backup of the file or not
        if (fileInfo == null) {
            System.out.println("The requested file: " + filePath + " doesn't exist in the database.");
        }
        
        data = new byte[fileInfo.getNumChunks()][];

        String fileID = fileInfo.getFileId();
        int numberChunks = fileInfo.getNumChunks();
        
        peer.addRestoreInitiator(fileID, this);

        for (int i = 0; i < numberChunks; i++) {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.GETCHUNK, peer.getID(), fileID, i);
            
            // Tries sending GETCHUNK message for MAX_TRIES
            for (int attempts = 0; attempts < MAX_TRIES; attempts++) {
            	// Sends message again if peer still hasn't received chunk
				if(data[i] == null) {
		            peer.getSubscribedGroup().sendMessageToRoot(msg,Util.ChannelType.MC);
					Util.randomDelay();
				} else {
					break;
				}
			}
            
            // Checks if chunk has been recovered.
			if (data[i] == null) {
				System.out.println("Failed to recover " + filePath + ".");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		peer.removerestoreInitiator(fileID);
		System.out.println(file + " has been restored successfully.");
    }
}
