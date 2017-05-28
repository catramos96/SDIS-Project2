package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FileSharing 
{
	public static void main(String[] args) throws IOException
	{		
		if(args.length != 4)
		{
			System.out.println("java peer.FileSharing <peer_id> <peer_address:defPort:mcPort:mdrPort:mdbPort> <remoteObjName> <tracker_address:port>");
			return;
		}

		int peer_id = Integer.parseInt(args[0]);
		String[] peer = addressVerification(args[1]);
        String remoteObjName = args[2];
		String[] tracker = addressVerification(args[3]);

		new Peer(peer_id,peer,tracker,remoteObjName);
	}

	private static String[] addressVerification(String arg) 
	{
		String[] parts_ap = arg.split(":");
		String[] parts = new String[5];

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
		else{
			parts = parts_ap;
		}

		return parts;
	}
}
