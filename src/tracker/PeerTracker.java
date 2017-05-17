package tracker;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class PeerTracker {

	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.out.println("java tracker.PeerTracker <myport>");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		
		try {
			new Tracker(port);
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
