package security;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import peer.Peer;

public class Encrypt{
	
	
	private static final String ALGORITHM = "AES";
	private static final int AES_KEY_SIZE = 256;
	private static final int RSA_KEY_SIZE = 1024;
	
	private static final String PUBLIC_KEY_FILE = "/public";
	private static final String PRIVATE_KEY_FILE = "/private";
	private static final String AES_KEY_FILE = "/aes";
	
	
	private Cipher aesCipher;
	private Cipher pkCipher;
	
	private  byte [] AESkey;
	private SecretKeySpec AESkeySpec;
	
	private Peer peer;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public Encrypt(Peer peer) throws Exception{
		 iniciateCipher();
		 this.peer = peer;
		 
		 try {
			getPublicKey();
			getPrivateKey();
		} catch (Exception e) {
			System.out.println("Client: No keys found generating new keys");
			generateRSAKeys();
			try {
				savePairKeys();
			} catch (Exception e1) {
				System.out.println("Client Warning: Can't save keys after terminal");
			}
		}
		 
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
		pkCipher = Cipher.getInstance("RSA");
	}
	
	
	private  void generateRSAKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator kgen = KeyPairGenerator.getInstance("RSA");
		kgen.initialize(RSA_KEY_SIZE);
		KeyPair pair = kgen.generateKeyPair();
		
		this.publicKey = pair.getPublic();
		this.privateKey = pair.getPrivate();
	}
	
	private void getPublicKey() throws Exception {
		File pKey = peer.getFileManager().getFile(PRIVATE_KEY_FILE);
		byte[] keyBytes = Files.readAllBytes(pKey.toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		publicKey = kf.generatePublic(spec);
	}
	
	private void getPrivateKey() throws Exception {
		File pKey = peer.getFileManager().getFile(PUBLIC_KEY_FILE);
		byte[] keyBytes = Files.readAllBytes(pKey.toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		privateKey = kf.generatePrivate(spec);
	}
	
	public void  savePairKeys () throws Exception {
		File publicK = peer.getFileManager().getFile(PUBLIC_KEY_FILE);
		File privateK = peer.getFileManager().getFile(PRIVATE_KEY_FILE);
		
		FileOutputStream fos = new  FileOutputStream(publicK);
		fos.write(publicKey.getEncoded());
		fos.flush();
		fos.close();
		
		fos = new FileOutputStream(privateK);
		fos.write(privateKey.getEncoded());
		fos.flush();
		fos.close();
		
	}
	
	private  void generateAESKey() throws NoSuchAlgorithmException  {
		KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
		kgen.init(AES_KEY_SIZE);
		SecretKey key = kgen.generateKey();
		AESkey = key.getEncoded();
		AESkeySpec = new SecretKeySpec(AESkey,"AES");
	}
		
	private void saveAESKey() throws Exception {
		 	// write AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
		    pkCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		    CipherOutputStream os = new CipherOutputStream(new FileOutputStream(aesK), pkCipher);
		    os.write(AESkey);
		    os.flush();
		    os.close();
		  }
	 
	private void loadAESKey() throws Exception {   
		 	// read AES key
			File aesK = peer.getFileManager().getFile(AES_KEY_FILE);
		    pkCipher.init(Cipher.DECRYPT_MODE, privateKey);
		    AESkey = new byte[AES_KEY_SIZE/8];
		    CipherInputStream is = new CipherInputStream(new FileInputStream(aesK), pkCipher);
		    is.read(AESkey);
		    AESkeySpec = new SecretKeySpec(AESkey, "AES");
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
