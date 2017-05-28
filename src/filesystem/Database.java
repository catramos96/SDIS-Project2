package filesystem;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class Database implements Serializable
{
    private static final long serialVersionUID = 1L;

    //chunks stored                             ("chunkKey", chunkInfo)
    private HashMap<String, ChunkInfo> storedChunks;
    //chunks created from backups               ("chunkKey", chunkInfo)
    private HashMap<String, ChunkInfo> sentChunks;
    //list of files whose backup was initiated  ("filepath", fileInfo)
    private HashMap<String, FileInfo> sentFiles;
    //list of restored files                    ("filepath", fileInfo)
    private HashMap<String, FileInfo> restoredFiles;

    public Database()
    {
        storedChunks = new HashMap<>();
        sentChunks = new HashMap<>();
        //sentChunksMapping = new HashMap<>();
        sentFiles = new HashMap<>();
        restoredFiles = new HashMap<>();
    }

    /*
    STOREDCHUNKS
     */

    // Methods related to chunks stored by peer
    public synchronized HashMap<String, ChunkInfo> getStoredChunks() {
        notify();
        return storedChunks;
    }

    public synchronized ChunkInfo getChunkInfo(String key) {
        ChunkInfo out = storedChunks.get(key);
        notify();
        return out;
    }

    public synchronized void saveChunkInfo(String key, ChunkInfo info) {
        storedChunks.put(key, info);
        notify();
    }

    public synchronized boolean hasChunkStored(String key) {
        boolean  out = storedChunks.containsKey(key);
        notify();
        return out;
    }

    public synchronized void removeStoredChunksFromFileID(String fileId)
    {
        for (ChunkInfo chunk : storedChunks.values()) {
            if (chunk.getFileId().equals(fileId)) {
                storedChunks.remove(chunk.getChunkKey());
            }
        }
    }

    public synchronized void removeStoredChunk(String chunkKey)
    {
        if(storedChunks.containsKey(chunkKey))
            storedChunks.remove(chunkKey);
    }

    //Returns a list of chunks with perceived replication degree higher than desired
    public synchronized ArrayList<ChunkInfo> getChunksHigherReplication() {
        ArrayList<ChunkInfo> chunkList = new ArrayList<ChunkInfo>();

        for (ChunkInfo chunk : storedChunks.values()) {
            if (chunk.getActualRepDeg() > chunk.getReplicationDeg()) {
                chunkList.add(chunk);
            }
        }

        notify();
        return chunkList;
    }

    public synchronized void updateActualRepDeg(int deg, String key) {
        if(storedChunks.containsKey(key)){
            storedChunks.get(key).setActualRepDeg(deg);
            notify();
        }
    }

    public synchronized int getDesiredRepDeg(String key){
        if (!hasChunkStored(key)) {
            notify();
            return 0;
        }

        ChunkInfo chunk = getChunkInfo(key);
        notify();
        return chunk.getReplicationDeg();
    }

    public synchronized boolean desiredReplication(String key) {
        if (!hasChunkStored(key)) {
            notify();
            return false;
        }

        ChunkInfo chunk = getChunkInfo(key);
        notify();
        return chunk.getActualRepDeg() >= chunk.getChunkNo();
    }

    public synchronized ArrayList<ChunkInfo> getChunksOrderedByReplication() {
        ArrayList<ChunkInfo> chunkList = new ArrayList<ChunkInfo>();

        chunkList.addAll(storedChunks.values());
        chunkList.sort(Comparator.comparing(ChunkInfo::getChunkKey));
        notify();
        return chunkList;
    }

    /*
    SENTCHUNKS
     */

    public synchronized void addSentChunk(String chunkKey,ChunkInfo chunk)
    {
        sentChunks.put(chunkKey,chunk);
    }

    public synchronized boolean hasSentChunk(String chunkKey)
    {
        return sentChunks.containsKey(chunkKey);
    }

    public synchronized boolean updateSentChunkRepDeg(String chunkKey,int actualRepDeg)
    {
        ChunkInfo ci = sentChunks.get(chunkKey);

        if(ci == null)
            return false;

        ci.setActualRepDeg(actualRepDeg);
        return true;
    }

    public synchronized void removeSentChunks(String fileId)
    {
        for (Map.Entry<String, ChunkInfo> c : sentChunks.entrySet()) {
            if (c.getValue().getFileId().equals(fileId))
                sentChunks.remove(c.getKey());
        }
    }
    
    public synchronized ArrayList<String> getChunksFromFile(String fileId) {
    	ArrayList<String> result = new ArrayList<String> ();
    	for (Map.Entry<String, ChunkInfo> c : sentChunks.entrySet()) {
            if (c.getValue().getFileId().equals(fileId))
                result.add(c.getKey());
        }
    	return result;
    }

    //Returns a list of chunks with perceived replication degree is bellow than desired
    public synchronized ArrayList<ChunkInfo> getSentChunksBellowRepDeg() {
        ArrayList<ChunkInfo> chunkList = new ArrayList<ChunkInfo>();

        for (ChunkInfo chunk : sentChunks.values()) {
            if (chunk.getActualRepDeg() < chunk.getReplicationDeg()) {
                chunkList.add(chunk);
            }
        }

        notify();
        return chunkList;
    }

    public ChunkInfo getSentChunkInfo(String chunkey) {
        return sentChunks.get(chunkey);
    }

    public void addFilesystem(String chunkKey) {
        int arp = sentChunks.get(chunkKey).getActualRepDeg();
        sentChunks.get(chunkKey).setActualRepDeg(arp+1);
    }

    public int getActualRepDeg(String chunkey) {
        return sentChunks.get(chunkey).getActualRepDeg();
    }

    public boolean removeFilesystem(String chunkKey) {
        int arp = sentChunks.get(chunkKey).getActualRepDeg();
        sentChunks.get(chunkKey).setActualRepDeg(arp-1);

        if(sentChunks.get(chunkKey).getActualRepDeg() < sentChunks.get(chunkKey).getReplicationDeg())
            return true;
        return false;
    }

    /*
    SENT FILES
     */
    public synchronized HashMap<String,FileInfo> getSentFiles() {
        HashMap<String,FileInfo> out = sentFiles;
        notify();
        return out;
    }

    public synchronized void addSentFile(String filepath, FileInfo fileinfo)
    {
        sentFiles.put(filepath, fileinfo);
        notify();
    }

    public synchronized void removeSentFile(String path) {
        if(path != null)
        {
            sentFiles.remove(path);
        }
        notify();
    }

    public synchronized boolean hasSentFile(String filepath) {
        return sentFiles.containsKey(filepath);
    }

    public synchronized boolean hasSentFileByFileID(String fileID) {
        for(Map.Entry<String, FileInfo> entry : sentFiles.entrySet())
            if(entry.getValue().getFileId().equals(fileID))
                return true;
        return false;
    }

    public synchronized FileInfo getFileInfo(String filepath) {
        FileInfo out = sentFiles.get(filepath);
        notify();
        return out;
    }

    /*
    RESTORED FILES
     */
    public synchronized void addRestoredFile(String filepath, FileInfo fileinfo)
    {
        restoredFiles.put(filepath, fileinfo);
        notify();
    }

    public synchronized void removeRestoredFile(String path) {
        if(path != null)
        {
            restoredFiles.remove(path);
        }
        notify();
    }

    public synchronized boolean hasRestoredFile(String filepath) {
        return restoredFiles.containsKey(filepath);
    }


    /*
    DISPLAYS
     */

    public void display() {

        System.out.println("\nINITIATOR PEER SENT FILES : ");
        for(String key : sentFiles.keySet()) {
            FileInfo value = sentFiles.get(key);
            System.out.println(" * Filepath " + key + " | FileID " + value.getFileId());
        }

        System.out.println("\nINITIATOR PEER SENT CHUNKS : ");
        for(String key : sentChunks.keySet()) {
            ChunkInfo value = sentChunks.get(key);
            System.out.println(" * Chunk key "+key+" | Size "+value.getData().length+" | repDeg "
                    +value.getReplicationDeg()+" | actual repDeg "+ value.getActualRepDeg());
        }

        System.out.println("\nINITIATOR PEER RESTORES : ");
        for(String key : restoredFiles.keySet()) {
            FileInfo value = restoredFiles.get(key);
            System.out.println(" * Filepath " + key + " | FileID " + value.getFileId());
        }

        System.out.println("\nPEER STORED CHUNKS : ");
        for(String key : storedChunks.keySet()) {
            ChunkInfo value = storedChunks.get(key);
            System.out.println(" * Chunk key "+key+" | Size "+value.getData().length+" | repDeg "
                    +value.getReplicationDeg()+" | actual repDeg "+ value.getActualRepDeg());
        }

    }
}