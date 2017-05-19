package protocols;

import filesystem.ChunkInfo;
import filesystem.FileInfo;
import message.ProtocolMessage;
import peer.Peer;
import resources.Util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BackupInitiator extends Thread
{
    private String filepath;
    private int    repDeg;
    private Peer   peer;
    private HashMap<String,ChunkBackupProtocol> protocols;

    public BackupInitiator(Peer peer, String filepath, int repDeg)
    {
        this.peer = peer;
        this.filepath = filepath;
        this.repDeg = repDeg;
        protocols = new HashMap<>();
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

        String fileID = peer.getFileManager().getFileIdFromFilename(filepath);

        //peer already backed up file, but it's a different version
        if(peer.getDatabase().hasSentFile(filepath))
        {
            FileInfo info = peer.getDatabase().getFileInfo(filepath);

            if(fileID.equals(info.getFileId()))
            {
                System.out.println("File already backed up!");
            }
            else
            {
                System.out.println("New version of file");
                //delete old chunks
                DeleteInitiator dt = new DeleteInitiator(peer, filepath);
                dt.start();
                try
                {
                    dt.join();	 //waits for chunks delete
                }
                catch (InterruptedException e) {
                    //Logs.exception("run", "BackupTrigger", e.toString());
                    e.printStackTrace();
                }

                //delete chunks and files from database
                peer.getDatabase().removeSentChunks(info.getFileId());
                peer.getDatabase().removeSentFile(filepath);
            }
        }

        //encrypt
        File toSend = new File(filepath);
        String tmpFileDir = peer.getFileManager().diskDIR+"/tmp/"+toSend.getName();
        File tmp = new File(tmpFileDir);
        
        try {
            tmp.createNewFile();
        }catch(IOException e1) {
        	e1.printStackTrace();
        	return;
        }
        
        try {
			peer.getEncrypt().encrypt(toSend, tmp);
		} catch (InvalidKeyException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
        //split file in chunks
        System.out.println("cyperSize: " + tmp.length());
        ArrayList<ChunkInfo> chunks = peer.getFileManager().splitFileInChunks(filepath,tmp);
        System.out.println("cyperSize: " + tmp.length());

        FileInfo fileinfo = new FileInfo(fileID,filepath,chunks.size(),repDeg);

        peer.addBackupInitiator(fileID,this);
        
        //add sentFile
        peer.getDatabase().addSentFile(filepath, fileinfo);

        for (ChunkInfo c: chunks)
        {
            //TODO confirm
            //resets stored messages of the record
            //peer.getChannelRecord().removeStoredMessages(c.getChunkKey());

            //starts recording sentChunks
            peer.getDatabase().addSentChunk(c.getChunkKey(),c);

            //message to send
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.PUTCHUNK,peer.getID(),c.getFileId(),c.getChunkNo(),repDeg,c.getData());

            //start chunk backup protocol
            ChunkBackupProtocol cbp = new ChunkBackupProtocol(peer.getSubscribedGroup(),msg);
            protocols.put(c.getChunkKey(),cbp);
            cbp.start();
        }

        //all protocols ended -> chunks (whose backup was initiated) actual replication degree must be updated
        //wait for all threads to finish
        for (Map.Entry<String, ChunkBackupProtocol> entry : protocols.entrySet())
        {
            try
            {
                entry.getValue().join();
                peer.getDatabase().updateSentChunkRepDeg(entry.getKey(),entry.getValue().getActualRepDeg());
            }
            catch (InterruptedException e) {
                //Logs.exception("run", "BackupTrigger", e.toString());
                e.printStackTrace();
            }
        }

        peer.removeBackupInitiator(fileID);
    }

    public void updateStores(String chunkKey, int senderId)
    {
       ChunkBackupProtocol cbp = protocols.get(chunkKey);
       if(cbp != null)
       {
           cbp.updateStores(senderId);
       }
    }
}
