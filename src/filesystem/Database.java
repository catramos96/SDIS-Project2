package filesystem;

import java.util.*;

public class Database {

    private HashMap<String, ChunkInfo> storedChunks;

    private HashMap<String, FileInfo> sentFiles;
    private HashSet<String>           sentFileId;

    private HashMap<String, Boolean> chunkSent;
    private HashMap<String, Boolean> putChunkSent;

    //private LinkedList<Message> deleteMessages;

    public Database() {
        storedChunks = new HashMap<String, ChunkInfo>();

        sentFiles = new HashMap<String, FileInfo>();
        sentFileId = new HashSet<String>();

        chunkSent = new HashMap<String, Boolean>();
        putChunkSent = new HashMap<String, Boolean>();

        //deleteMessages = new LinkedList<Message>();
    }

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

    public synchronized boolean chunkOnDB(String key) {
        boolean  out = storedChunks.containsKey(key);
        notify();
        return out;
    }

    public synchronized boolean desiredReplication(String key) {
        if (!chunkOnDB(key)) {
            notify();
            return false;
        }

        ChunkInfo chunk = getChunkInfo(key);
        notify();
        return chunk.getAtualRepDeg() >= chunk.getChunkNo();
    }

    public synchronized void updateReplicationDegree(int change, String key) {
        storedChunks.get(key).setReplicationDeg(change);
        notify();
    }

    public synchronized ArrayList<ChunkInfo> getChunksOrderedByReplication() {
        ArrayList<ChunkInfo> chunkList = new ArrayList<ChunkInfo>();

        chunkList.addAll(storedChunks.values());
        chunkList.sort(Comparator.comparing(ChunkInfo::getChunkKey));
        notify();
        return chunkList;
    }

    // Returns a list of chunkId for chunks with perceived replication degree higher than desired
    public synchronized ArrayList<ChunkInfo> getChunksHigherReplication() {
        ArrayList<ChunkInfo> chunkList = new ArrayList<ChunkInfo>();

        for (ChunkInfo chunk : storedChunks.values()) {
            if (chunk.getAtualRepDeg() > chunk.getReplicationDeg()) {
                chunkList.add(chunk);
            }
        }

        notify();
        return chunkList;
    }

    // Methods related to files sent by peer
    public synchronized HashMap<String, FileInfo> getSentFiles() {
        HashMap<String, FileInfo> out = sentFiles;
        notify();
        return out;
    }

    public synchronized FileInfo getFileData(String filepath) {
        FileInfo out = sentFiles.get(filepath);
        notify();
        return out;
    }

    public synchronized void saveStoredFile(String filepath, FileInfo fileinfo) {
        sentFileId.add(fileinfo.getFileId());
        sentFiles.put(filepath, fileinfo);
        notify();
    }

    public synchronized boolean hasStoredFileWithFilename(String filepath) {
        for(Map.Entry<String, FileInfo> entry : sentFiles.entrySet())
           if(entry.getValue().getFilename().equals(filepath))
               return true;
        return false;
    }

    public synchronized boolean sentFileId(String fileid) {
        boolean out  = sentFileId.contains(fileid);
        notify();
        return out;
    }

    public synchronized void registerChunkSent(String chunkId) {
        chunkSent.put(chunkId, true);
        notify();
    }

    public synchronized void clearChunkSent(String chunkId) {
        chunkSent.put(chunkId, false);
        notify();
    }

    public synchronized boolean chunkAlreadySent(String chunkId) {
        if (chunkSent.get(chunkId) != null){
            boolean out = chunkSent.containsKey(chunkId);
            notify();
            return out;
        }

        notify();
        return false;
    }

    public synchronized void removeChunk(String chunkId) {
        chunkSent.remove(chunkId);
        storedChunks.remove(chunkId);
        notify();
    }


    public synchronized void removeFile(String path) {
        if(path != null)
        {
            sentFiles.remove(path);
            sentFileId.remove(path);
        }
        notify();
    }

    public synchronized void listenPutChunkFlag(String key) {
        putChunkSent.put(key, false);
        notify();
    }

    public synchronized void removePutChunkFlag(String key) {
        putChunkSent.remove(key);
        notify();
    }

    public synchronized void markPutChunkSent(String key) {
        if (putChunkSent.get(key) != null) {
            putChunkSent.put(key, true);
        }
        notify();
    }

    public synchronized boolean getPutChunkSent(String key) {
        Boolean response = putChunkSent.get(key);

        if (response != null) {
            notify();
            return response;
        }

        return false;
    }
/*
    public synchronized void addDeleteMessage(Message msg) {
        // store only the last 10 messages
        if (deleteMessages.size() >= 30) {
            deleteMessages.removeFirst();
        }

        deleteMessages.add(msg);
    }

    public LinkedList<Message> getDeleteMessages() {
        return deleteMessages;
    }
*/
    // List ChunkInfo Information
    public synchronized String ListChunks() {

        String out = "";

        for(String key : storedChunks.keySet()) {
            ChunkInfo value = storedChunks.get(key);
            out +=key + " with size " + value.getData().length +  " bytes and replication "+ value.getAtualRepDeg();
            out +="\n";
        }
        notify();
        return out;
    }
}