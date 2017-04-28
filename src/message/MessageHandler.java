package message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import peer.Peer;
import resources.Util;

public class MessageHandler extends Thread{
	
	private Peer peer = null;
	
	public MessageHandler(byte[] message, Peer peer){
		
		this.peer = peer;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try {
			String header = reader.readLine();
			String[] parts = header.split("\\s");
			
			if(Util.TopologyMessageType.valueOf(parts[0]) != null){
				TopologyMessage msg = TopologyMessage.parseMessage(parts);
				handleTopologyMessage(msg);
			}
			else if(Util.ProtocolMessageType.valueOf(parts[0]) != null){
				
			}
			else{
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public void handleTopologyMessage(TopologyMessage msg){
	
	 switch (msg.getType()) {
		case ROOT:{
			System.out.println("RECEIVED ROOT MSG");
			break;
		}
		case PARENT:{
			System.out.println("RECEIVED PARENT MSG");
			break;
		}
		case SUBSCRIBER:{
			System.out.println("RECEIVED SUBSCRIBER MSG");
			break;
		}
		case NEWSUBSCRIBER:{
			System.out.println("RECEIVED NEWSUBSCRIBER MSG");
			break;
		}
		case REMSUBSCRIBER:{
			System.out.println("RECEIVED REMSUBSCRIBER MSG");
			break;
		}
		case MOVSUBSCRIBER:{
			System.out.println("RECEIVED MOVSUBSCRIBER MSG");
			break;
		}
		default:{
			System.out.println("RECEIVED UNKNOWN MSG");
			break;
		}
	}
		
	}

}
