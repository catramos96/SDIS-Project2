package protocols;

import filesystem.FileInfo;
import message.ProtocolMessage;
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
            System.out.println("File not backed up");
            return;
        }

        FileInfo info = peer.getDatabase().getFileInfo(filepath);

        //create message
        String fileId = info.getFileId();
        ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.DELETE,peer.getID(),info.getFileId());

        //send message twice because UDP is not reliable
        peer.getSubscribedGroup().sendMessageToRoot(msg,Util.ChannelType.MC);
        System.out.println("Delete");
        //Logs.sentMessageLog(msg);

        Util.randomDelay();

        peer.getSubscribedGroup().sendMessageToRoot(msg,Util.ChannelType.MC);
        System.out.println("Delete");
        //Logs.sentMessageLog(msg);

        //delete restores
        String dir = peer.getFileManager().diskDIR + Util.RESTORES_DIR + info.getFilename();
        peer.getFileManager().deleteFile(dir);

        //delete entries from database (sent chunks and files)
        peer.getDatabase().removeSentChunks(fileId);
        peer.getDatabase().removeSentFile(filepath);

        //TODO confirm
        //reset mdr ?
        peer.getChannelRecord().resetChunkMessages(fileId);

        /*message = "Delete successful!";
        Logs.log(message);*/
    }
}