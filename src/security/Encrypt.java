package security;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt{
	
	
	private static final String ALGORITHM = "AES";
	private static final int AES_KEY_SIZE = 256;
	
	static byte [] AESkey;
	static SecretKeySpec AESkeySpec;
	static Cipher aesCipher;
	
	
	
	public  void iniciateCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(ALGORITHM);
	}
	
	public  void generateKey() throws NoSuchAlgorithmException  {
		KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
		kgen.init(AES_KEY_SIZE);
		SecretKey key = kgen.generateKey();
		AESkey = key.getEncoded();
		AESkeySpec = new SecretKeySpec(AESkey,"AES");
	}
	
	public void encrypt(File in, File out) throws IOException, InvalidKeyException{
		System.out.println(AESkey.length);
		System.out.println(AESkeySpec.getEncoded().length);
		aesCipher.init(Cipher.ENCRYPT_MODE, AESkeySpec);
		
		CipherInputStream  dataIN = new CipherInputStream( new FileInputStream(in), aesCipher);
	    FileOutputStream  dataOUT = new FileOutputStream(out);
		
		int i;
		byte[] data = new byte[1024];
		
		
		while ((i = dataIN.read(data)) != -1){
			dataOUT.write(data, 0, i);		
		}
		
		dataIN.close();
		dataOUT.close();
	}
	
	public void decrypt(File in, File out) throws InvalidKeyException, IOException {
		aesCipher.init(Cipher.DECRYPT_MODE, AESkeySpec);
		
		CipherInputStream  dataIN = new CipherInputStream( new FileInputStream(in), aesCipher);
	    FileOutputStream  dataOUT = new FileOutputStream(out);
	    
		int i;
		byte[] data = new byte[1024];
		
		
		while ((i = dataIN.read(data)) != -1){
			dataOUT.write(data, 0, i);		
		}
		
		dataIN.close();
		dataOUT.close();		
	}
	
}
