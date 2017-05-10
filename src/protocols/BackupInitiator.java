package protocols;

import filesystem.Chunk;
import message.ProtocolMessage;
import peer.Peer;
import resources.Util;

import java.io.File;
import java.util.ArrayList;

public class BackupInitiator extends Thread
{
    private String filename;
    private int    repDeg;
    private Peer   peer;

    public BackupInitiator(Peer peer, String filename, int repDeg)
    {
        this.peer = peer;
        this.filename = filename;
        this.repDeg = repDeg;

    }

    private boolean fileExist()
    {
        //verifies original file existence
        File f = new File(this.filename);
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

        //TODO: verifies if this file was already backed up
        //TODO: verifies if the file was modified -> delete protocol

        //split file in chunks
        ArrayList<Chunk> chunks = peer.getFileManager().splitFileInChunks(filename);

        //TODO: record files at db

        for (Chunk c: chunks)
        {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.PUTCHUNK,peer.getID(),c.getFileId(),c.getChunkNo(),repDeg,c.getData());

            //TODO : send message
        }

    }
}
