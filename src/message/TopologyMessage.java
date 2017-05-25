package message;
import network.Subscriber;
import resources.Logs;
import resources.Util;
import resources.Util.TopologyMessageType;

public class TopologyMessage extends Message{
	Subscriber subscriber = null;
	String key = null;
	int subscriberN;        //Number of subscribers info to send
	int pagination;         //pagination os subscribers
	
	/**
	 * ONLINE			<address> <defPort> <mcPort> <mdrPort> <mdbPort>		<CRLF><CRLF>
     * GETONLINE		<number>                                                <CRLF><CRLF>
     * GET 				<key> <number> <paginationN>							<CRLF><CRLF>
	 * PUT				<key> <address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 * SUBSCRIBER 		<key> <address> <defPort> <mcPort> <mdrPort> <mdbPort>	<CRLF><CRLF>
	 */
	
	public TopologyMessage(Util.TopologyMessageType type, Subscriber s){
		if(type.name().equals("ONLINE")){
			this.type = type;
			this.subscriber = s;
		}
	}
	
	public TopologyMessage(Util.TopologyMessageType type, Subscriber subscriber, String key){
		if(type.name().equals("PUT") || type.name().equals("SUBSCRIBER")){
			this.type = type;
			this.key = key;
			this.subscriber = subscriber;
		}
	}

	public TopologyMessage(Util.TopologyMessageType type, String key, int number, int pagination){
		if(type.name().equals("GET")){
			this.type = type;
			this.key = key;
			this.subscriberN = number;
			this.pagination = pagination;
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

		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.ONLINE) != 0 &&
            ((TopologyMessageType)type).compareTo(Util.TopologyMessageType.GETONLINE) != 0)
		    content += key + " ";

		if(((TopologyMessageType)type).compareTo(Util.TopologyMessageType.GETONLINE) == 0 ||
                ((TopologyMessageType)type).compareTo(Util.TopologyMessageType.GET) == 0) {
            content += subscriberN + " ";
            if (((TopologyMessageType) type).compareTo(Util.TopologyMessageType.GET) == 0)
                content += pagination + " ";
        }
		else
			content += subscriber.getAddress().getHostAddress() + " " + 
						subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
						subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
		
		content += Util.LINE_SEPARATOR + Util.LINE_SEPARATOR;
		
		return content.getBytes();
	}
	
	public static TopologyMessage parseMessage(byte[] message){
		TopologyMessage parsed = null;
		
		String parts[] = new String(message).split("\\s");
		
		Util.TopologyMessageType type_rcv;
		
		if((type_rcv = Util.TopologyMessageType.valueOf(parts[0])) != null){
		
			Subscriber s1 = null;
			int p1,p2,p3,p4;
			int subscriersN = -1, pagination = -1;
			String key = null;

            if(((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.ONLINE) != 0 &&
                    ((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.GETONLINE) != 0)
				key = parts[1];

            if(((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.GETONLINE) == 0 ||
                    ((TopologyMessageType)type_rcv).compareTo(Util.TopologyMessageType.GET) == 0) {
                subscriersN = Integer.parseInt(parts[2]);
                if (((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.GET) == 0)
                    pagination = Integer.parseInt(parts[3]);
            }
			else{
				p1 = Integer.parseInt(parts[2]);
				p2 = Integer.parseInt(parts[3]);
				p3 = Integer.parseInt(parts[4]);
				p4 = Integer.parseInt(parts[5]);
				s1 = new Subscriber(parts[1],p1,p2,p3,p4);
			}

			if(key == null){
                if(s1 != null)              //ONLINE
                    parsed = new TopologyMessage(type_rcv,s1);
                else                        //GETONLINE
                    parsed = new TopologyMessage(type_rcv,subscriersN);
            }
            else{
			    if(s1 == null)
			        parsed = new TopologyMessage(type_rcv,key,subscriersN,pagination);
			    else
			        parsed = new TopologyMessage(type_rcv,s1,key);
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

    public int getPagination(){
	    return pagination;
    }

    public String getKey(){
        return key;
    }
}