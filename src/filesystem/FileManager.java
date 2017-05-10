package filesystem;

import resources.Util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class FileManager
{
    private int peerID;
    public String diskDIR = Util.PEERS_DIR;

    public FileManager(int id){
        this.peerID = id;
        checkDirectories();
    }

    private void checkDirectories()
    {
        //init directories
        File dir = new File(new String(diskDIR));
        if(!dirExists(dir))
        {
            dir.mkdir();
        }

        //peer directory name
        diskDIR += "Peer"+ peerID;
        dir = new File(new String(diskDIR));
        if(!dirExists(dir))
        {
            dir.mkdir();
        }

        //chunk directory name
        String name = diskDIR + Util.CHUNKS_DIR;
        dir = new File(new String(name));
        if(!dirExists(dir))
        {
            dir.mkdir();
        }

        //restore directory name
        name = diskDIR + Util.RESTORES_DIR;
        dir = new File(new String(name));
        if(!dirExists(dir))
        {
            dir.mkdir();
        }

        //local files directory name
        name = diskDIR + Util.LOCAL_DIR;
        dir = new File(new String(name));
        if(!dirExists(dir))
        {
            dir.mkdir();
        }
    }

    private boolean dirExists(File dir){
        return dir.exists() && dir.isDirectory();
    }

    private String hashFileId(File file) throws NoSuchAlgorithmException
    {
        //filename, last modification, ownwer
        String textToEncrypt = file.getName() + file.lastModified() + peerID;
        return sha256(textToEncrypt);
    }

    private String sha256(String textToEncrypt) throws NoSuchAlgorithmException
    {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha.digest(textToEncrypt.getBytes());

        return DatatypeConverter.printHexBinary(hash);
    }

    public ArrayList<Chunk> splitFileInChunks(String filename)
    {
        ArrayList<Chunk> chunkList = new ArrayList<>();	//list of chunks created for this file
        File file = new File(filename);	//open file

        //verifies file existence
        if(file.exists())
        {
            try
            {
                String fileID = hashFileId(file);
                int numChunks = (int) (file.length() / Util.CHUNK_MAX_SIZE) + 1;
                byte[] bytes = Files.readAllBytes(file.toPath());
                int byteCount = 0;

                for(int i = 0; i < numChunks; i++)
                {
                    int length = Util.CHUNK_MAX_SIZE;

                    if (i == numChunks-1)
                    {
                        length = (int) (bytes.length % Util.CHUNK_MAX_SIZE);
                    }
                    byteCount = 0;
                    byte[] data = new byte[length];

                    for (int j = i*Util.CHUNK_MAX_SIZE; j < Util.CHUNK_MAX_SIZE*i+length; j++)
                    {
                        data[byteCount] = bytes[j];
                        byteCount++;
                    }
                    Chunk c = new Chunk(fileID, i, data);
                    chunkList.add(c);
                }

            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
           System.out.println("Error splitting file");

        return chunkList;
    }
}
