package security;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class teste {

	public static void main(String[] args) {
		testSSLSockets(Boolean.parseBoolean(args[0]));
		//testEncrypt();
	}


	public static void testSSLSockets(boolean swit) {
		String cypherSuites [] = new String[0];
		
		
		
		
		if(swit)
		try {
			SSLlistenerServer server =  new SSLlistenerServer(4455,cypherSuites);
			server.start();
		} catch (IOException e) {
			System.out.println("SERVER: failed to create server");
			e.printStackTrace();
			return;
		} 
		
		if(!swit)
		try {
			SSLlistenerClient client = new SSLlistenerClient("localhost", 4455, cypherSuites);
			client.start();
			
		} catch (UnknownHostException e) {
			System.out.println("CLIENT: failed to create client");
			e.printStackTrace();
			return;
		}
		 
	}

	public static void testEncrypt() {
		Encrypt t = new Encrypt();
		try {
			t.iniciateCipher();
			t.generateKey();

			String path = "C:\\\\Users\\pmds3\\OneDrive\\Documentos\\GitHub\\SDIS-Project2\\src\\";
			String javaPath = path.replace("\\", "/");
			File in = new File(path+"ff.txt");
			File out = new File(path+"ff.encrypted");
			t.encrypt(in,out);

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
}
