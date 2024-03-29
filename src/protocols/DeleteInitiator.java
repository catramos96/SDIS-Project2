package protocols;

import client.Notification;
import filesystem.FileInfo;
import message.ProtocolMessage;
import message.TopologyMessage;
import peer.Peer;
import resources.Util;

/**
 * Peer initiator response to client request for delete a file.
 * @attribute Peer peer - initiator peer
 * @attribute String filename - file filename
 * @attribute String message - response to client
 */
public class DeleteInitiator extends Thread{

    private String filepath;
    private Peer   peer;

    /**
     * constructor
     * @param peer
     * @param filepath
     */
    public DeleteInitiator(Peer peer, String filepath){
        this.peer = peer;
        this.filepath = filepath;
    }

    /**
     * Thread execution
     */
    @Override
    public void run()
    {
        //verifies if this file was backed up at this peer
        if(!peer.getDatabase().hasSentFile(filepath))
        {
            //message = filename + " not backed up by this peer!"
        	String notificationMsg = "File not backed up";
            System.out.println(notificationMsg);
            (new Thread(new Notification("DELETE " + peer.getID(), notificationMsg))).start();
            return;
        }

        FileInfo info = peer.getDatabase().getFileInfo(filepath);

        //create message
        String fileId = info.getFileId();
        ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.DELETED,peer.getID(),info.getFileId());   
        TopologyMessage msgTracker = new TopologyMessage(Util.TopologyMessageType.DELETE,peer.getDatabase().getChunksFromFile(info.getFileId()));
        msgTracker.buildMessage();
      //send message twice because UDP is not reliable
        for(int i = 0; i < 2; i++) {
            peer.getSubscribedGroup().sendMessageToSubscribers(msg,Util.ChannelType.MC);
            peer.getSubscribedGroup().sendMessageToTracker(msgTracker);
            System.out.println("Delete");
            //Logs.sentMessageLog(msg);
            Util.randomDelay();
        }

        //delete restores
        String dir = peer.getFileManager().diskDIR + Util.RESTORES_DIR + info.getFilename();
        peer.getFileManager().deleteFile(dir);

        //delete entries from database (sent chunks, sent chunks mappings, files, restores)
        peer.getDatabase().removeSentChunks(fileId);
        peer.getDatabase().removeSentFile(filepath);
        //peer.getDatabase().removeChunkMapping(fileId);
        peer.getDatabase().removeRestoredFile(filepath);

        /*message = "Delete successful!";
        Logs.log(message);*/
        

        String notificationMsg = filepath  + " ended!";
        (new Thread(new Notification("DELETE " + peer.getID(), notificationMsg))).start();
        
    }
}
