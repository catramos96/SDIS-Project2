package protocols;

import peer.Peer;
import resources.Util;
import filesystem.FileInfo;
import message.ProtocolMessage;

/**
 * Class RestoreInitiator used to initiate the restore protocol of a file.
 */
public class RestoreInitiator extends Thread {
    private Peer peer;
    private String filePath;

    /**
     * Constructor of RestoreInitiator
     */
    public RestoreInitiator(Peer peer, String filePath) {
        this.peer = peer;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        FileInfo fileInfo = peer.getDatabase().getFileData(filePath);
        
    	// Checks whether peer has requested the backup of the file or not
        if (fileInfo == null) {
            System.out.println("The requested file: " + filePath + " doesn't exist in the database.");
        }

        String fileID = fileInfo.getFileId();
        int numberChunks = fileInfo.getNumChunks();

        for (int i = 0; i < numberChunks; i++) {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.GETCHUNK, peer.getID(), fileID, i);
            new ChunkRestoreProtocol(peer.getSubscribedGroup(), msg).start();
        }
    }
}
