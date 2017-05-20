package security;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import peer.Peer;

public class Encrypt{
	
	
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int AES_KEY_SIZE = 256;

	private static final String AES_KEY_FILE = "/aes";
	
	
	private Cipher aesCipher;

	private  byte [] AESkey;
	private SecretKeySpec AESkeySpec;
	private SecretKey key;
	private Peer peer;
	
	private IvParameterSpec iv;
	
	public Encrypt(Peer peer) throws Exception{
		 iniciateCipher();
		 this.peer = peer;
		 
		try {
			loadAESKey();
		} catch (Exception e2) {
			System.out.println("Client: No AES key generating new key");
			generateAESKey();
			saveAESKey();
		}
	}
	
	private  void iniciateCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(ALGORITHM);
		SecureRandom random = new SecureRandom();
	    byte tmp[] = new byte[16];//generate random 16 byte long
	    random.nextBytes(tmp);
	    iv = new IvParameterSpec(tmp);
	}
		
	private  void generateAESKey() throws NoSuchAlgorithmException  {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_KEY_SIZE);
		key = kgen.generateKey();
		AESkey = key.getEncoded();
		AESkeySpec = new SecretKeySpec(AESkey, ALGORITHM);
	}
		
	private void saveAESKey() throws Exception {
		 	// write AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
			if(!aesK.exists()){
				aesK.createNewFile();
			}
			
		    FileOutputStream out = new FileOutputStream(aesK);
		    ObjectOutputStream oout = new ObjectOutputStream(out);
		    try {
		      oout.writeObject(key);
		      oout.flush();
		    } finally {
		      oout.close();
		    }
		  }
	 
	private void loadAESKey() throws Exception {   
		 	// read AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
		    AESkey = new byte[AES_KEY_SIZE/8];
		    FileInputStream in = new FileInputStream(aesK);
			ObjectInputStream oin = new ObjectInputStream(in);
		    try {
		      key = (SecretKey) oin.readObject();
		    } finally {
		      oin.close();
		    }
		  } 
	
	public void encrypt(File in, File out) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		System.out.println(AESkey.length);
		System.out.println(AESkeySpec.getEncoded().length);
		System.out.println(aesCipher);
		aesCipher.init(Cipher.ENCRYPT_MODE, key,iv);
		
		FileOutputStream  dataOUT = new FileOutputStream(out);
		
	    byte[] data = new byte[(int) in.length()];
	    data = Files.readAllBytes(in.toPath());
	    dataOUT.write(aesCipher.doFinal(data));
	    
	    System.out.println("encryptedSize"+ out.length() );
	    dataOUT.flush();
		dataOUT.close();
	}
	
	public void decrypt(File in, File out) throws InvalidKeyException, IOException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		aesCipher.init(Cipher.DECRYPT_MODE, key,iv);
	    FileOutputStream  dataOUT = new FileOutputStream(out);
		byte[] data = Files.readAllBytes(in.toPath());
		System.out.println("decrypt file SIZE:" + data.length);
		dataOUT.write(aesCipher.doFinal(data));
		dataOUT.flush();
		dataOUT.close();		
	}
	
}
