package protocols;

import filesystem.ChunkInfo;
import filesystem.FileInfo;
import message.ProtocolMessage;
import peer.Peer;
import resources.Util;

import java.io.File;
import java.util.ArrayList;

public class BackupInitiator extends Thread
{
    private String filepath;
    private int    repDeg;
    private Peer   peer;

    public BackupInitiator(Peer peer, String filepath, int repDeg)
    {
        this.peer = peer;
        this.filepath = filepath;
        this.repDeg = repDeg;

    }

    private boolean fileExist()
    {
        //verifies original file existence
        File f = new File(this.filepath);
        if(!f.exists())
        {
            System.out.println("File doesn't exist!");
            return false;
        }
        return true;
    }

    @Override
    public void run()
    {
        if(!fileExist())
            return;

        if(peer.getDatabase().hasStoredFileWithFilename(filepath))
        {
            //TODO execute delete protocol
        }

        //split file in chunks
        ArrayList<ChunkInfo> chunks = peer.getFileManager().splitFileInChunks(filepath);

        String fileID = peer.getFileManager().getFileIdFromFilename(filepath);
        FileInfo fileinfo = new FileInfo(fileID,filepath,chunks.size(),repDeg);

        //starts recording file
        peer.getDatabase().saveStoredFile(filepath, fileinfo);

        for (ChunkInfo c: chunks)
        {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.PUTCHUNK,peer.getID(),c.getFileId(),c.getChunkNo(),repDeg,c.getData());
            new ChunkBackupProtocol(peer.getSubscribedGroup(),msg).start();
        }

    }
}