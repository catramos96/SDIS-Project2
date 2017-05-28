package filesystem;

import resources.Util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Class responsible for representing a chunk.
 * Each chunk has a fileId and a chunk number associated.
 * Each chunk has also its content (data) and the replication degree actual and the desired.
 * This class is serialized.
 *
 * @attribute int chunkNo - chunk number
 * @attribute String fileId - fileId associated
 * @attribute byte[] data - information saved at the disk
 * @attribute int replicationDeg - replication degree desired
 * @attribute ArrayList<Integer> peers - List of peers with the chunk associated (count = actual replication degree)
 */
public class ChunkInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int     chunkNo = -1;
    private String  fileId = null;
    private byte[]  data = null;
    private int     replicationDeg = 0;	//Desired
    private int     actualRepDeg = 0;
    private Calendar expirationDate = null;


    //private ArrayList<Integer> peers = new ArrayList<Integer>();

    /**
     * Constructor
     * @param fileNo
     * @param chunkNo
     * @param replicationDeg
     */
    public ChunkInfo(String fileNo, int chunkNo, int replicationDeg){
        this.fileId = fileNo;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.actualRepDeg = 0;
        this.expirationDate = Calendar.getInstance();
        expirationDate.setTime(new Date());
        expirationDate.add(Calendar.DATE, Util.CHUNK_EXPIRATION);

    }

    /**
     * Constructor
     * @param fileId
     * @param chunkNo
     * @param data
     */
    public ChunkInfo(String fileId, int chunkNo, byte[] data){
        this.setChunkNo(chunkNo);
        this.setFileId(fileId);
        this.setData(data);
        this.actualRepDeg = 0;

        this.expirationDate = Calendar.getInstance();
        expirationDate.setTime(new Date());
        expirationDate.add(Calendar.DATE, Util.CHUNK_EXPIRATION);

    }

	/*
	 * Gets and sets
	 */

    public int getReplicationDeg(){
        return replicationDeg;
    }

    public void setReplicationDeg(int rep){ replicationDeg = rep; }

    public int getActualRepDeg(){ return actualRepDeg; }

    public void setActualRepDeg(int rep){ actualRepDeg = rep; }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getChunkKey() {
        return (this.chunkNo+this.fileId);
    }

    public boolean hasExpired(){

       return (expirationDate.getTime().before(new Date()));
    }

    public void renovateDate(){
        expirationDate.setTime(new Date());
        expirationDate.add(Calendar.DATE, Util.CHUNK_EXPIRATION);
    }
}