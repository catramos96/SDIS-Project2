package tracker;
import message.TopologyMessage;
import network.Subscriber;
import resources.Logs;
import resources.Util;

import java.util.ArrayList;


public class MessageTrackerHandler extends Thread {
	private Tracker tracker = null;
	private Subscriber sender = null;
	
	public MessageTrackerHandler(byte[] message, Subscriber sender, Tracker tracker){
		
		if(!tracker.authorizedIP(sender.getAddress().getHostAddress())) {
			return;
		}
		this.tracker = tracker;
		this.sender = sender;
		
		String content = new String(message);

		int firstSpace = content.indexOf(new String(" "));
		String type = content.substring(0,firstSpace);
	
		
		if(Util.isTopologyMessageType(type)){
			TopologyMessage msg = TopologyMessage.parseMessage(message);
			handleTopologyMessage(msg);
		}
		else{
			System.out.println(content);
		}
	}
	
	public void handleTopologyMessage(TopologyMessage msg){
		
		Logs.receivedTopologyMessage(msg);
		
		switch (msg.getType()) {

		    //Update last access of subscriber
            case ONLINE:{
                tracker.registerSubscriber(msg.getSubscriber());
                break;
            }
            //I need the last n subscribers that were online
            case GETONLINE:{
                ArrayList<Subscriber> lastOnline = tracker.getLastAccess(msg.getSubscriberN());
                TopologyMessage m;

               int size = lastOnline.size();
               int i = 0,j = 0;

               //because it has a limit of MAX_N_SUBSCRIBERS that can be sent per message
               while(i < size){
                   if((j = i+Util.MAX_N_SUBSCRIBERS) >= size)
                       j = size;

                   m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBERS,new ArrayList<Subscriber>(lastOnline.subList(i,j)));
                   tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
                   Logs.sentTopologyMessage(m);

                   i = j;
               }

                break;
            }
			case PUT: {
				tracker.putDHT(msg.getKey(),msg.getSubscriber());
				break;
			}
			case GET: {
				ArrayList<Subscriber> subs = tracker.getDHT(msg.getKey(),msg.getSubscriberN(),msg.getPagination());
				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.SUBSCRIBERS,subs);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case CHECK:{
				ArrayList<Integer> repDegs = new ArrayList<Integer>();
				for(String k : msg.getKeys()){
					repDegs.add(tracker.checkDHT(k));
				}

				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.INFO,msg.getKeys(),repDegs);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case REMOVE:{
				ArrayList<String> keys = new ArrayList<String>();	keys.add(msg.getKey());
				ArrayList<Integer> reps = new ArrayList<Integer>(); reps.add(tracker.remSubscriberDHT(msg.getKey(),msg.getSubscriber()));
				TopologyMessage m = new TopologyMessage(Util.TopologyMessageType.INFO,keys,reps);
				tracker.getChannel().send(m.buildMessage(),sender.getAddress(),sender.getDefPort());
				Logs.sentTopologyMessage(m);
				break;
			}
			case DELETE:{
				ArrayList<String> keys = msg.getKeys();
				for (String c : keys) {
				tracker.deleteDHT(c);
				}
				break;
			}
            default:{
                System.out.println("RECEIVED UNKNOWN MSG");
                break;
            }
	    }
	}
}
