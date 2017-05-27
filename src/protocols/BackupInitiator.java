package protocols;

import filesystem.ChunkInfo;
import filesystem.FileInfo;
import message.ProtocolMessage;
import message.TopologyMessage;
import peer.Peer;
import resources.Util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

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

        //request peers to tracker
        peer.getSubscribedGroup().resetSubscribers();
        TopologyMessage msg = new TopologyMessage(Util.TopologyMessageType.GETONLINE,repDeg*2);
        peer.getSubscribedGroup().sendMessageToTracker(msg);

        String fileID = peer.getFileManager().getFileIdFromFilename(filepath);

        //peer already backed up file, but it's a different version
        if(peer.getDatabase().hasSentFile(filepath)) {
            FileInfo info = peer.getDatabase().getFileInfo(filepath);

            if (fileID.equals(info.getFileId())) {
                System.out.println("File already backed up!");
                peer.getSubscribedGroup().resetSubscribers();
                return;
            } else {
                System.out.println("New version of file");
                //delete old chunks
                DeleteInitiator dt = new DeleteInitiator(peer, filepath);
                dt.start();
                try {
                    dt.join();     //waits for chunks delete
                } catch (InterruptedException e) {
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
		} catch (InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //split file in chunks
        ArrayList<ChunkInfo> chunks = peer.getFileManager().splitFileInChunks(filepath,tmp);
        FileInfo fileinfo = new FileInfo(fileID,filepath,chunks.size(),repDeg);

        //peer.addBackupInitiator(fileID,this);
        
        //add sentFile
        peer.getDatabase().addSentFile(filepath, fileinfo);

        for (ChunkInfo c: chunks)
        {
            //starts recording sentChunks
            c.setReplicationDeg(repDeg);
            peer.getDatabase().addSentChunk(c.getChunkKey(),c);

            sendChunk(c);
        }

        //all protocols ended -> chunks (whose backup was initiated) actual replication degree must be updated
        //wait for all threads to finish
        waitProtocols();

        System.out.println(" - BACKUP SUCCESSFUL - ");

        if( tmp.delete()) {
            System.out.println("Backup: temporary files deleted");
        };

    }

    public void sendChunk(ChunkInfo c)
    {
        peer.getDatabase().startChunkMapping(c.getChunkKey());

        //message to send
        ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.PUTCHUNK,peer.getID(),c.getFileId(),c.getChunkNo(),c.getReplicationDeg(), peer.getMySubscriptionInfo().getAddress().getHostAddress(), peer.getMySubscriptionInfo().getMdbPort(), c.getData());

        //start chunk backup protocol
        ChunkBackupProtocol cbp = new ChunkBackupProtocol(peer.getDatabase(),peer.getSubscribedGroup(),msg);
        protocols.put(c.getChunkKey(),cbp);
        cbp.start();
    }

    public void waitProtocols()
    {
        for (Map.Entry<String, ChunkBackupProtocol> entry : protocols.entrySet())
        {
            waitProtocol(entry.getKey(),entry.getValue());
        }
    }

    public void waitProtocol(String chunkKey, ChunkBackupProtocol prot)
    {
        try
        {
            prot.join();
            peer.getDatabase().updateSentChunkRepDeg(chunkKey,peer.getDatabase().getActualRepDeg(chunkKey));
        }
        catch (InterruptedException e) {
            //Logs.exception("run", "BackupTrigger", e.toString());
            e.printStackTrace();
        }
    }
}
