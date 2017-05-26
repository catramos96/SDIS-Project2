package message;
import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.TopologyMessageType;

import java.util.ArrayList;

public class TopologyMessage extends Message{
	Subscriber subscriber = null;
	ArrayList<Subscriber> subscribersGroup = null;
	int subscriberN = -1;
	
	/**
	 * ONLINE			<address> <defPort> <mcPort> <mdrPort> <mdbPort>		<CRLF><CRLF>
     * GETONLINE		<number>                                                <CRLF><CRLF>
	 * SUBSCRIBERS 		                                                        <CRLF>
	 *     				<address> <defPort> <mcPort> <mdrPort> <mdbPort>        <CRLF>
     *     				<address> <defPort> <mcPort> <mdrPort> <mdbPort>        <CRLF>
	 *     				 ...                                                    <CRLF><CRLF>
	 */
	
	public TopologyMessage(Util.TopologyMessageType type, Subscriber s){
		if(type.name().equals("ONLINE")){
			this.type = type;
			this.subscriber = s;
		}
	}

	public TopologyMessage(Util.TopologyMessageType type, ArrayList<Subscriber> subs){
        if(type.name().equals("SUBSCRIBERS")){
            this.type = type;
            this.subscribersGroup = subs;
        }
    }

    public TopologyMessage(Util.TopologyMessageType type, int number){
        if(type.name().equals("GETONLINE")){
            this.type = type;
            this.subscriberN = number;
        }
    }
	
	@Override
	public byte[] buildMessage(){
		
		String content = type.name() + " ";

		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.ONLINE) == 0){
            content += subscriber.getAddress().getHostAddress() + " " +
                    subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
                    subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
        }
		else if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.GETONLINE) == 0) {
            content += subscriberN + " ";
        }
		else {
			content += Util.LINE_SEPARATOR;
            for (Subscriber s : subscribersGroup)
                content += " " + s.getAddress().getHostAddress() + " " +
                        s.getDefPort() + " " + s.getMcPort() + " " +
                        s.getMdrPort() + " " + s.getMdbPort() + " " +
                        Util.LINE_SEPARATOR;
            content+= Util.LINE_SEPARATOR;
        }

        if(((TopologyMessageType)type).compareTo(TopologyMessageType.SUBSCRIBERS) != 0)
		    content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;

		return content.getBytes();
	}
	
	public static TopologyMessage parseMessage(byte[] message){
		TopologyMessage parsed = null;

		String p[] = new String(message).split(" " + Util.LINE_SEPARATOR + " ");
        String header = p[0];

        String parts[] = new String(header).split("\\s");

        Util.TopologyMessageType type_rcv;
		
		if((type_rcv = Util.TopologyMessageType.valueOf(parts[0])) != null){

            ArrayList<Subscriber> subs = null;
			Subscriber s1 = null;
			int p1,p2,p3,p4;
			int subscriersN = -1;

            if(((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.GETONLINE) == 0) {
                subscriersN = Integer.parseInt(parts[1]);

                parsed = new TopologyMessage(type_rcv,subscriersN);
            }
			else if(((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.ONLINE) == 0 ){
				p1 = Integer.parseInt(parts[2]);
				p2 = Integer.parseInt(parts[3]);
				p3 = Integer.parseInt(parts[4]);
				p4 = Integer.parseInt(parts[5]);
				s1 = new Subscriber(parts[1],p1,p2,p3,p4);

				parsed = new TopologyMessage(type_rcv,s1);
			}
            else{
                subs = new ArrayList<Subscriber>();
                String subParts[];

                int j = 1;

                while(j < p.length){
                    subParts = p[j].split("\\s");
                    p1 = Integer.parseInt(subParts[1]);
                    p2 = Integer.parseInt(subParts[2]);
                    p3 = Integer.parseInt(subParts[3]);
                    p4 = Integer.parseInt(subParts[4]);
                    subs.add(new Subscriber(subParts[0],p1,p2,p3,p4));
                    j++;
                }

                parsed = new TopologyMessage(type_rcv,subs);
            }
		}
		else{
			Logs.notTopologyMessage();
		}
		
		return parsed;
	}
	
	/*
	 * Gets and Sets
	 */

	public Util.TopologyMessageType getType(){
		return (TopologyMessageType) type;
	}
	
	public Subscriber getSubscriber(){
		return subscriber;
	}

	public int getSubscriberN(){
	    return subscriberN;
    }

    public ArrayList<Subscriber> getSubscribersGroup(){
	    return subscribersGroup;
    }
}