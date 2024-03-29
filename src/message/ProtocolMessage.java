package message;

import resources.Util;
import resources.Util.ProtocolMessageType;

import java.io.*;


/**
 * Class ProtocolMessageType
 * Used to build and parse the messages received by the communication channels
 * 
 * @attribute type - Type of the message, it belongs to the enum MessageType
 * @attribute version - Version of the sender peer protocols
 * @attribute senderId - Sender peer identification number
 * @attribute fileId - File identification
 * @attribute chunkNo - ChunkInfo identification number
 * @attribute replicationDeg - Desired replication degree of the chunk associated
 * @attribute address - Address of the sender peer (ENHANCEMENT)
 * @attribute port - Port of the sender peer (ENHANCEMENT)
 * @attribute body - Content of the chunk associated
 */
public class ProtocolMessage extends Message
{
	//ProtocolMessageType information
	private int senderId = -1;
	private String fileId = null;
	private int chunkNo = -1;
	private int replicationDeg = -1;
	private String address = null;		//For enhancement
	private int port = -1;				//For enhancement
	private byte[] body = null;
	
	//Special characters for message construction
	private static final char CR = 0xD;								
	private static final char LF = 0xA;
	public static final String LINE_SEPARATOR = "" + CR + LF;
	
	/**
	 * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 * 
	 * PUTCHUNK	<Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> 	<CRLF><CRLF>	<Body>
	 * STORED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * CHUNK 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>	<Body>
	 * DELETED 	<Version> <SenderId> <FileId> 								<CRLF><CRLF>
	 * REMOVED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * 
	 * Enhancement Messages:
	 * 
	 * GETCHUNKENH <Version> <SenderId> <FileId> <ChunkNo> <Address> <Port> <CRLF><CRLF>
	 * GOTCHUNKENH <Version> <SenderId> <FileId> <ChunkNo>					<CRLF><CRLF>
	 * GETINITIATOR <Version> <SenderId> <FileId>								<CRLF><CRLF>
	 * INITIATOR	<Version> <SenderId> <FileId>	
	 */
	
	/**
	 * Constructor of ProtocolMessageType for the type PUTCHUNK
	 * @param type - Type of the message, it has to be a PUTCHUNK
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - ChunkInfo identification number
	 * @param replicationDeg - Desired replication degree of the associated chunk
	 * @param address - Address of the sender
	 * @param port - Port of the sender
	 * @param body - Content of the associated chunk
	 */
	public ProtocolMessage(ProtocolMessageType type, int senderId, String fileId, int chunkNo, int replicationDeg, String address, int port, byte[] body)
	{
        if (!type.name().equals("PUTCHUNK"))
            System.out.println("error creating message");
            //Logs.wrongMessageConstructor(type);

		this.type = type;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
		this.address = address;
		this.port = port;
		this.body = body;
	}
	
	/**
	 * Constructor of ProtocolMessageType for the types STORED, REMOVED, GOTCHUNKENH
	 * @param type - Type of the message, it has to be one of the types mentioned above
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - ChunkInfo identification number
	 */
	public ProtocolMessage(ProtocolMessageType type, int senderId, String fileId, int chunkNo)
	{
		if(!(type.name().equals("STORED")|| type.name().equals("REMOVED") || type.name().equals("GOTCHUNKENH")))//Logs.wrongMessageConstructor(type);
            System.out.println("error creating message");

		this.type = type;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
	}
	
	/**
	 * Constructor of ProtocolMessageType for the type CHUNK
	 * @param type - Type of the message, it has to be a CHUNK
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - ChunkInfo identification number
	 * @param body - Content of the associated chunk
	 */
	public ProtocolMessage(ProtocolMessageType type, int senderId, String fileId, int chunkNo, byte[] body)
	{
		if(!type.name().equals("CHUNK")){
            System.out.println("error creating message");
			//Logs.wrongMessageConstructor(type);
		}
		else
		{
			this.type = type;
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
			this.body = body;
		}
	}

	/**
	 * Constructor of ProtocolMessageType for the types DELETED, GETINITIATOR, INITIATOR.
	 * @param type - Type of the message, it has to be one of the types mentioned above
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 */
	public ProtocolMessage(ProtocolMessageType type, int senderId, String fileId) {
		if(!(type.name().equals("DELETED") || type.name().equals("GETINITIATOR") || type.name().equals("INITIATOR"))){
            System.out.println("error creating message");
			//Logs.wrongMessageConstructor(type);
		}
		else
		{
			this.type = type;
			this.senderId = senderId;
			this.fileId = fileId;
		}
	}
	
	/**
	 * Constructor of ProtocolMessageType for the type GETCHUNK
	 * @param type - Type of the message, it has to be a GETCHUNK
	 * @param senderId - Sender identification
	 * @param fileId - File identification
	 * @param chunkNo - ChunkInfo identification number
	 * @param address - Address of the sender
	 * @param port - Port of the sender
	 */
	public ProtocolMessage(ProtocolMessageType type, int senderId, String fileId, int chunkNo, String address, int port){
		if(!type.name().equals("GETCHUNK")){
            System.out.println("error creating message");
		}
		else
		{
			this.type = type;
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
			this.address = address;
			this.port = port;
		}
	}
	

	/**
	 * Creates a new message, depending on the attributes of the class object.
	 * 
	 * @return The message to be sent in byte[]
	 */
	@Override
	public byte[] buildMessage() {
		
		String content = type.name() + " " + senderId + " " + fileId + " ";
		
		if(((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.DELETED) != 0 || 
			((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.GETINITIATOR) != 0 || 
			((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.INITIATOR) != 0 )
			content += chunkNo + " ";
		
		if(((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.PUTCHUNK) == 0) {
			content += replicationDeg + " " + address + " " + port + " ";
			
		}
		
		if(((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.GETCHUNK) == 0)
			content += address + " " + port + " ";
		
		content += LINE_SEPARATOR + LINE_SEPARATOR;
		
		if(((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.PUTCHUNK) == 0 || 
			((ProtocolMessageType)type).compareTo(Util.ProtocolMessageType.CHUNK) == 0)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try 
			{
				baos.write(content.getBytes());
				baos.write(body);
			} 
			catch (IOException e) 
			{
				//Logs.exception("buildMessage", "ProtocolMessageType", e.toString());
				e.printStackTrace();
			}
			
			byte[] a = baos.toByteArray();
			return a;
		}
		
		return content.getBytes();
	}
	
	/*
	 * GETS & SETS
	 */

	/**
	 * Function to get the type of the message.
	 * @return type - ProtocolMessageType type
	 */
	public Util.ProtocolMessageType getType() {
		return (ProtocolMessageType) type;
	}

	/**
	 * Function to get the chunk number associated with the message.
	 * @return ChunkInfo associated to the message
	 */
	public int getChunkNo() {
		return chunkNo;
	}

	/**
	 * Function to get the desired replication degree associated with the message.
	 * @return replicationDegree
	 */
	public int getReplicationDeg() {
		return replicationDeg;
	}

	/**
	 * Function to get the file identification associated with the message.
	 * @return File identification
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * Function to get the chunk's content associated with the message.
	 * @return Chunks content.
	 */
	public byte[] getBody() {
		return body;
	}

	/**
	 * Function to get the sender identification associated with the message.
	 * @return Sender identification
	 */
	public int getSenderId() {
		return senderId;
	}
	
	/**
	 * Function to get the address of the sender associated with the message.
	 * @return Address
	 */
	public String getAddress(){
		return address;
	}
	
	/**
	 * Function to get the port of the sender associated with the message.
	 * @return Port
	 */
	public int getPort(){
		return port;
	}
	
	/**
	 * Receives a message in byte[] and parses it filling the respective attributes
	 * of a new ProtocolMessageType object. It also checks if the actual peers version that is
	 * passed as a parameter (peerVersion) is compatible with the message protocol version.
	 * 
	 * @param message
	 * @return ProtocolMessageType Object
	 */

	public static ProtocolMessage parseMessage(byte[] message)
	{
		ProtocolMessage parsed = null;

		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try
		{
			String header = reader.readLine();
			String[] parts = header.split("\\s");

			ProtocolMessageType type_rcv = validateMessageType(parts[0]);

            System.out.println("message type "+type_rcv.name());

			//Common to all messages types
			int senderId_rcv = Integer.parseInt(parts[1]);
			String fileId_rcv = parts[2];

			//Exception for the types DELETED, GETINITIATOR, INITIATOR.
			int chunkNo_rcv = -1;
			if(type_rcv.compareTo(Util.ProtocolMessageType.DELETED) != 0 && type_rcv.compareTo(Util.ProtocolMessageType.GETINITIATOR) != 0 && type_rcv.compareTo(Util.ProtocolMessageType.INITIATOR) != 0 )
				chunkNo_rcv = Integer.parseInt(parts[3]);

			//Exception for type PUTCHUNK
			int replicationDeg_rcv = -1;
			String address_rcv = null;
			int port_rcv = -1;
			if(type_rcv.compareTo(Util.ProtocolMessageType.PUTCHUNK) == 0){
				replicationDeg_rcv = Integer.parseInt(parts[4]);
				address_rcv = parts[5];
				port_rcv = Integer.parseInt(parts[6]);
			}
			
			// Exception for the type GETCHUNK
			if (type_rcv.compareTo(Util.ProtocolMessageType.GETCHUNK) == 0) {
				address_rcv = parts[4];
				port_rcv = Integer.parseInt(parts[5]);
			}

			//Removes the last sequences of white spaces (\s) and null characters (\0)
			//String msg_received = (new String(packet.getData()).replaceAll("[\0 \\s]*$", ""));
			int offset = header.length() + ProtocolMessage.LINE_SEPARATOR.length()*2;
			int bodySize = message.length - offset;
			byte[] body = new byte[bodySize];
			System.arraycopy(message, offset, body, 0, bodySize);
			
			

			//Creates the message with the respective attributes
			if(type_rcv.compareTo(Util.ProtocolMessageType.DELETED) == 0 || type_rcv.compareTo(Util.ProtocolMessageType.GETINITIATOR) == 0 || type_rcv.compareTo(Util.ProtocolMessageType.INITIATOR) == 0)
				parsed = new ProtocolMessage(type_rcv,senderId_rcv,fileId_rcv);
			else if(type_rcv.compareTo(Util.ProtocolMessageType.STORED) == 0 || type_rcv.compareTo(Util.ProtocolMessageType.REMOVED) == 0 || type_rcv.compareTo(Util.ProtocolMessageType.GOTCHUNKENH) == 0)
				parsed = new ProtocolMessage(type_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv) ;
			else if(type_rcv.compareTo(Util.ProtocolMessageType.PUTCHUNK) == 0){
				parsed = new ProtocolMessage(type_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,replicationDeg_rcv, address_rcv, port_rcv, body);
			}
			else if(type_rcv.compareTo(Util.ProtocolMessageType.CHUNK) == 0)
				parsed = new ProtocolMessage(type_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,body);
            else if(type_rcv.compareTo(Util.ProtocolMessageType.GETCHUNK) == 0)
				parsed = new ProtocolMessage(type_rcv,senderId_rcv,fileId_rcv,chunkNo_rcv,address_rcv,port_rcv);

			reader.close();
			stream.close();
		} 
		catch (IOException e) 
		{
			//Logs.exception("parseMessage", "ProtocolMessageType", e.toString());
			e.printStackTrace();
		}

		return parsed;
	}
	
	/*
	 * Validations
	 */
	
	/**
	 * Validates the type of the message.
	 * @param type - Type of the message
	 * @return MessageType corresponding to the type, if it's null, then the type of the message is not valid.
	 */
	private static Util.ProtocolMessageType validateMessageType(String type) 
	{
		return Util.ProtocolMessageType.valueOf(type);
	}
	
}
