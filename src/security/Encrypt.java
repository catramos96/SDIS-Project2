package security;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
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
	
	
	 public static void main(String[] args) {
	       try {
			iniciateCipher();
			generateKey();
			File in = new File("ff.txt");
			File out = new File("ff.encrypted");
			encrypt(in,out);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       
	    }
	
	public static void iniciateCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(ALGORITHM);
	}
	
	private static  void generateKey() throws NoSuchAlgorithmException  {
		KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
		kgen.init(AES_KEY_SIZE);
		SecretKey key = kgen.generateKey();
		AESkey = key.getEncoded();
		AESkeySpec = new SecretKeySpec(AESkey,"AES");
	}
	
	public static void encrypt(File in, File out) throws IOException, InvalidKeyException{
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
