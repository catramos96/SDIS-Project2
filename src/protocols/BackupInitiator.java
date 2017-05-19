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
        }
        
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

        String fileID = peer.getFileManager().getFileIdFromFilename(filepath);
        FileInfo fileinfo = new FileInfo(fileID,filepath,chunks.size(),repDeg);
        
        //starts recording file
        peer.getDatabase().saveStoredFile(filepath, fileinfo);

        for (ChunkInfo c: chunks)
        {
            ProtocolMessage msg = new ProtocolMessage(Util.ProtocolMessageType.PUTCHUNK,peer.getID(),c.getFileId(),c.getChunkNo(),repDeg,c.getData());
            new ChunkBackupProtocol(peer.getSubscribedGroup(),msg).start();
        }

        System.out.println("Backup info :");
        System.out.println(peer.getDatabase().ListFiles());
       if( tmp.delete()) {
    	   System.out.println("Backup: temporary files deleted");
       };
    }
}
