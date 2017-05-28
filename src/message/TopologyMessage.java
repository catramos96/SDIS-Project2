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

	String key = null;
	ArrayList<String> keys = null;
	ArrayList<Integer> repDegs = null;
	int pagination = -1;
	
	/**
	 * ONLINE			<address> <defPort> <mcPort> <mdrPort> <mdbPort>		    <CRLF><CRLF>
     *
     * GETONLINE		<number>                                                    <CRLF><CRLF>
     *
	 * SUBSCRIBERS 		                                                    <CRLF>
	 *     				<address> <defPort> <mcPort> <mdrPort> <mdbPort>    <CRLF>
     *     				<address> <defPort> <mcPort> <mdrPort> <mdbPort>    <CRLF>
	 *     				 ...                                                        <CRLF><CRLF>
     *
     * PUT              <key> <address> <port1> <port2> <port3> <port4>             <CRLF><CRLF>
     *
     * GET              <key> <numberPeers> <pagination>                            <CRLF><CRLF>
     *
     * CHECK                                                                <CRLF>
     *                  <key>                                               <CRLF>
     *                  <key>                                                        <CRLF><CRLF>
     * REMOVE           <key> <address> <port1> <port2> <port3> <port4>              <CRLF><CRLF>
     *
     * DELETE                                                               <CRLF>
     *                  <key>                                               <CRLF>
     *                  <key>                                                        <CRLF><CRLF>
     *
     * INFO                                                                 <CRLF>
     *                  <key> <repDeg>                                      <CRLF>
     *                  <key> <repDeg>                                                <CRLF><CRLF>
	 */

	public TopologyMessage(Util.TopologyMessageType type, String key, Subscriber s){
        if(type.name().equals("PUT") || type.name().equals("REMOVE")){
            this.type = type;
            this. key = key;
            this.subscriber = s;
        }
        else
            Logs.errorMsg("Topology message wrong format");
    }

    public TopologyMessage(Util.TopologyMessageType type, String key, int nPeers, int pagination){
        if(type.name().equals("GET")){
            this.type = type;
            this.key = key;
            this.subscriberN = nPeers;
            this.pagination = pagination;
        }
        else
            Logs.errorMsg("Topology message wrong format");
    }

    public TopologyMessage(Util.TopologyMessageType type, ArrayList<String> keys, ArrayList<Integer> repDegs){
        if(type.name().equals("INFO")){
            if(keys.size() != repDegs.size()){
                Logs.errorMsg("Info message with keys and repDegs that do not match in size");
                return;
            }
            this.type = type;
            this.keys = keys;
            this.repDegs = repDegs;
        }
        else
            Logs.errorMsg("Topology message wrong format");
    }

    public <T> TopologyMessage(Util.TopologyMessageType type, ArrayList<T> elems){

        this.type = type;

        if(type.name().equals("DELETE") || type.name().equals("CHECK")){
            this.keys = (ArrayList<String>)elems;
        }
        else if(type.name().equals("SUBSCRIBERS")){
            this.subscribersGroup = (ArrayList<Subscriber>)elems;
        }
        else
            Logs.errorMsg("Topology message wrong format");
    }

	public TopologyMessage(Util.TopologyMessageType type, Subscriber s){
		if(type.name().equals("ONLINE")){
			this.type = type;
			this.subscriber = s;
		}
        else
            Logs.errorMsg("Topology message wrong format");
	}

    public TopologyMessage(Util.TopologyMessageType type, int number){
        if(type.name().equals("GETONLINE")){
            this.type = type;
            this.subscriberN = number;
        }
        else
            Logs.errorMsg("Topology message wrong format");
    }
	
	@Override
	public byte[] buildMessage(){
		
		String content = type.name() + " ";
        if(key == null && keys == null) {
            if (((TopologyMessageType) type).compareTo(Util.TopologyMessageType.ONLINE) == 0) {
                content += subscriber.getAddress().getHostAddress() + " " +
                        subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
                        subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
            } else if (((TopologyMessageType) type).compareTo(Util.TopologyMessageType.GETONLINE) == 0) {
                content += subscriberN + " ";
            } else {
                content += Util.LINE_SEPARATOR;
                for (Subscriber s : subscribersGroup)
                    content += " " + s.getAddress().getHostAddress() + " " +
                            s.getDefPort() + " " + s.getMcPort() + " " +
                            s.getMdrPort() + " " + s.getMdbPort() + " " +
                            Util.LINE_SEPARATOR;
                content += Util.LINE_SEPARATOR;
            }
        }
        else{
            if(key != null){

                content += key + " ";

                if(((TopologyMessageType) type).compareTo(Util.TopologyMessageType.PUT) == 0 ||
                    (((TopologyMessageType) type).compareTo(Util.TopologyMessageType.REMOVE) == 0)){
                    content += subscriber.getAddress().getHostAddress() + " " +
                            subscriber.getDefPort() + " " + subscriber.getMcPort() + " " +
                            subscriber.getMdrPort() + " " + subscriber.getMdbPort() + " ";
                }
                else if(((TopologyMessageType) type).compareTo(Util.TopologyMessageType.INFO) == 0){
                    content += subscriberN + " ";
                }
                else if(((TopologyMessageType) type).compareTo(Util.TopologyMessageType.GET) == 0){
                    content += subscriberN + " " + pagination + " ";
                }
            }
            else{
                content += Util.LINE_SEPARATOR;

                if(repDegs == null){
                    for(String k : keys){
                        content += " " + k + " " + Util.LINE_SEPARATOR;
                    }
                }
                else{
                    if(keys.size() != repDegs.size()){
                        Logs.errorMsg("Wrong message construction");
                        return new String("").getBytes();
                    }

                    for(int i = 0; i < keys.size(); i++){
                        content += " " + keys.get(i) + " " + repDegs.get(i) + " " + Util.LINE_SEPARATOR;
                    }

                }

                content += Util.LINE_SEPARATOR;
            }
        }

        if (((TopologyMessageType) type).compareTo(TopologyMessageType.SUBSCRIBERS) != 0 &&
            ((TopologyMessageType) type).compareTo(TopologyMessageType.DELETE) != 0 &&
                ((TopologyMessageType) type).compareTo(TopologyMessageType.CHECK) != 0 &&
                ((TopologyMessageType) type).compareTo(TopologyMessageType.INFO) != 0)
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
            else if(((TopologyMessageType)type_rcv).compareTo(TopologyMessageType.SUBSCRIBERS) == 0 ){
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
            else if(((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.PUT) == 0 ||
                    (((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.REMOVE) == 0)){
                p1 = Integer.parseInt(parts[3]);
                p2 = Integer.parseInt(parts[4]);
                p3 = Integer.parseInt(parts[5]);
                p4 = Integer.parseInt(parts[6]);
                s1 = new Subscriber(parts[2],p1,p2,p3,p4);

                parsed = new TopologyMessage(type_rcv,parts[1],s1);
            }
            else if(((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.GET) == 0) {
                parsed = new TopologyMessage(type_rcv,parts[1],Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
            }
            else if(((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.DELETE) == 0 ||
                    ((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.CHECK) == 0) {
                ArrayList<String> keys = new ArrayList<String>();

                int j = 1;

                while(j < p.length){
                    if(j == p.length-1)
                        p[j] = p[j].replace(" " + Util.LINE_SEPARATOR + Util.LINE_SEPARATOR,"");
                    keys.add(p[j]);
                    j++;
                }

                parsed = new TopologyMessage(type_rcv,keys);
            }
            else if(((TopologyMessageType) type_rcv).compareTo(Util.TopologyMessageType.INFO) == 0){

                ArrayList<String> keys = new ArrayList<String>();
                ArrayList<Integer> reps = new ArrayList<Integer>();

                int j = 1;
                while(j < p.length){
                    String subparts[] = p[j].split("\\s");
                    keys.add(subparts[0]) ;
                    reps.add(Integer.parseInt(subparts[1]));
                    j++;
                }

                parsed = new TopologyMessage(type_rcv,keys,reps);
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

    public String getKey(){return key;}

	public ArrayList<String> getKeys(){return keys;}

	public int getPagination(){return pagination;}

    public ArrayList<Subscriber> getSubscribersGroup(){
	    return subscribersGroup;
    }

    public ArrayList<Integer> getRepDegs(){
        return repDegs;
    }

    @Override
    public String toString(){
        String s = new String("");

        if(key == null && keys == null) {
            if (type.equals(Util.TopologyMessageType.ONLINE))
                s += subscriber.toString();
            else if (type.equals(Util.TopologyMessageType.GETONLINE))
                s += subscriberN;
            else {
                s += "\n";
                for (Subscriber sub : subscribersGroup)
                    s += "           " + sub.toString() + "\n";
            }
        }
        else{
            if(type.equals(TopologyMessageType.PUT) || type.equals(TopologyMessageType.REMOVE))
                s+= subscriber.toString();
            else if(type.equals(TopologyMessageType.GET))
                s+= subscriberN + " " + pagination;
            else if(type.equals(TopologyMessageType.DELETE) ||(type.equals(TopologyMessageType.CHECK))){
                s+="\n";
                for(String k : keys){
                    s += "           " + k + "\n";
                }
            }
            else if( type.equals(TopologyMessageType.INFO)){
                s+="\n";
                for(int i = 0; i < keys.size(); i++){
                    s+="             RepDeg: " + repDegs.get(i) + " Key: " + keys.get(i) + "\n";
                }
            }
        }

        return s;
    }
}