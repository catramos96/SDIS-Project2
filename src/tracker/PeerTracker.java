package tracker;

import java.io.IOException;

public class PeerTracker {

	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.out.println("java tracker.PeerTracker <myport>");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		
		new Tracker(port);
	}
}