package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FileSharing 
{

	public static void main(String[] args) throws IOException
	{		
		if(args.length != 3)
		{
			System.out.println("java peer.FileSharing <peer_id> <remoteObjName> <address:port>");
			return;
		}

		int peer_id = Integer.parseInt(args[0]);
		String remoteObjName = args[1];
		String[] root = addressVerification(args[2]);

		new Peer(peer_id,root,remoteObjName);
		
		/*String[] parts_mc = addressVerification(args[3]);
		String[] parts_mdb = addressVerification(args[4]);
		String[] parts_mdr = addressVerification(args[5]);*/

		//new Peer(protocol_version,peer_id,remoteObjName,parts_mc,parts_mdb,parts_mdr);	
	}

	private static String[] addressVerification(String arg) 
	{
		String[] parts_ap = arg.split(":");
		String[] parts = new String[2];

		//localhost
		if(parts_ap.length == 1)
		{
			try{
				parts[0] = InetAddress.getLocalHost().getHostName();
			} 
			catch (UnknownHostException e) {
				//Logs.exception("addressVerification", "BackupSystem", e.toString());
				e.printStackTrace();
			}
			parts[1] = parts_ap[0];
		}
		else
			parts = parts_ap;

		return parts;
	}

}
