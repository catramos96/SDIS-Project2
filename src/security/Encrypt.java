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

	private static final String AES_KEY_FILE = "/aes.txt";
	private static final String IV = "/iv.txt";
	
	
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
	}
		
	private  void generateAESKey() throws NoSuchAlgorithmException  {
		SecureRandom random = new SecureRandom();
		byte tmp[] = new byte[16];//generate random 16 byte long
		random.nextBytes(tmp);
		iv = new IvParameterSpec(tmp);
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_KEY_SIZE);
		key = kgen.generateKey();
		AESkey = key.getEncoded();
		AESkeySpec = new SecretKeySpec(AESkey, ALGORITHM);
	}
		
	private void saveAESKey() throws Exception {
		 	// write AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
			if(!aesK.exists()) {
				aesK.createNewFile();
			}
		    FileOutputStream out = new FileOutputStream(aesK);
			out.write(key.getEncoded());
			out.flush();
			out.close();

			File ivFile = peer.getFileManager().getFile(IV);
			if(!ivFile.exists()) {
			ivFile.createNewFile();
			}
			out = new FileOutputStream(ivFile);
			out.write(iv.getIV());
			out.flush();
			out.close();


		  }
	 
	private void loadAESKey() throws Exception {   
		 	// read AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
		    AESkey = new byte[AES_KEY_SIZE/8];
		    FileInputStream in = new FileInputStream(aesK);
		 	in.read(AESkey);
			key = new SecretKeySpec(AESkey,0,16,"AES");
			in.close();

			// read AES key
			File ivFile = peer.getFileManager().getFile(IV);
			in = new FileInputStream(ivFile);
			byte tmp[] = new byte[16];
			in.read(tmp);
			iv = new IvParameterSpec(tmp);
			in.close();
		  } 
	
	public void encrypt(File in, File out) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
		aesCipher.init(Cipher.ENCRYPT_MODE, key,iv);
		FileOutputStream  dataOUT = new FileOutputStream(out);
		
	    byte[] data = new byte[(int) in.length()];
	    data = Files.readAllBytes(in.toPath());
	    dataOUT.write(aesCipher.doFinal(data));
	    dataOUT.flush();
		dataOUT.close();
	}
	
	public void decrypt(File in, File out) throws InvalidKeyException, IOException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		aesCipher.init(Cipher.DECRYPT_MODE, key,iv);
	    FileOutputStream  dataOUT = new FileOutputStream(out);
		byte[] data = Files.readAllBytes(in.toPath());
		dataOUT.write(aesCipher.doFinal(data));
		dataOUT.flush();
		dataOUT.close();		
	}
	
}
