package resources;

import message.ProtocolMessage;
import message.TopologyMessage;
import network.Subscriber;

public class Logs {

	public static void MyAddress(Subscriber s){
		System.out.println("ME:      " + s.toString());
	}
	
	//Handling Topology Messages

	/**
	 * Function that displays the information about a topology message received
	 * @param message
	 */
	public static void receivedTopologyMessage(TopologyMessage message){
		String print = new String("RECEIVED - TOPOLOGY:" + message.getType().toString() + ": ");
		print += message.toString();
		System.out.println(print);
	}

	/**
	 * Function that displays the information about a topology message sent
	 * @param message
	 */
	public static void sentTopologyMessage(TopologyMessage message){
		String print = new String("SENT     - TOPOLOGY:" + message.getType().toString() + ": ");
		print += message.toString();
		System.out.println(print);
	}
	//Topology Messages

	/**
	 * Function that displays the information about a message that is not of the type topology
	 */
	public static void notTopologyMessage(){
		errorMsg("TOPOLOGY: Message is not of type Topology!");
	}

	/**
	 * Function that display an error with a message
	 * @param msg
	 */
	public static void errorMsg(String msg){

		System.out.println("ERROR    - " + msg);
	}

	public static void newMsg(String msg){
		System.out.println("NEW      - " + msg);
	}

	public static void remMsg(String msg){
		System.out.println("REMOVED  - " + msg);
	}

	public static void delMsg(String msg){
		System.out.println("DELETED  - " + msg);
	}

	/*
	 * Messages
	 */

	/**
	 * Function that displays the information about a message that was sent.
	 * @param msg - Message sent
	 */
	public static void sentMessageLog(ProtocolMessage msg){
		System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}

	/**
	 * Function that displays the information about a message that was received.
	 * @param msg - Message received
	 */
	public static void receivedMessageLog(ProtocolMessage msg){
		System.out.println("(Received) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
	}

	/**
	 * Function that displays the information about a wrong construction of a message of a certain type.
	 * @param type - Type of the message with the wrong construction
	 */
	public static void wrongMessageConstructor(ProtocolMessage type){
		System.out.println("Wrong constructor for type message " + type.toString());
	}

	/*
	 * Files
	 */

	/**
	 * Function that displays the error on trying to obtain the file identification associated with a file name.
	 * @param filename - Name of the file
	 */
	public static void errorFileId(String filename){
		System.out.println("Error searching for fileId of "+filename);
	}

	/**
	 * Function that displays the error on trying to restore a file with the given file name.
	 * @param filename - Name of the file
	 */
	public static void errorRestoringFile(String filename){
		System.out.print("Erro restoring file " +  filename);
	}

	/**
	 * Function that displays the error on trying to open a file with a given file name.
	 * @param filename - Name of the file
	 */
	public static void errorOpeningFile(String filename){
		System.out.println("Error opening "+filename+" file.");
	}

	/**
	 * Function that displays the error on trying to find a file with a certain file name.
	 * @param filename - Name of the file
	 */
	public static void errorFindingFile(String filename){
		System.out.println("Error "+filename+" not found.");
	}

	/**
	 * Function that displays the success of trying to restore a file with a given file name.
	 * @param filename - Name of the file
	 */
	public static void fileRestored(String filename){
		System.out.println("File "+filename+" restored");
	}

	/**
	 * Function that displays the success of trying to restore a chunk of a file.
	 * @param chunkNo - Identification number of the chunk that was restored
	 */
	public static void chunkRestored(int chunkNo){
		System.out.println("Chunk Number "+chunkNo+" restored");
	}
	/**
	 * Function that displays the information about the lack of disk space.
	 */
	public static void diskWithoutSpace(){
		System.out.println("Disk without space to store the chunk");
	}

	/**
	 * Function that displays the information about the existance of a chunk.
	 */
	public static void chunkAlreadyExists(){
		System.out.println("Chunk Already Exists");
	}

	/*
	 * Directory
	 */
	public static void creatingDir(String string) {
		System.out.println("Creating directory "+ string);
	}

	/*
	 * Protocols
	 */

	/**
	 * Function that displays the information about the initiation of a certain protocol.
	 * @param name - Name of the protocol initiated
	 */
	public static void initProtocol(String name){
		System.out.println(name+" Protocol initiated...");
	}

	/**
	 * Function that displays the information about the number of tries that the chunk backup protocol is trying
	 * to store a certain chunk number.
	 * @param nr - Number of tries
	 * @param chunkNo - Chunk identification number
	 */
	public static void tryNrStoreChunk(int nr,int chunkNo){
		System.out.println("Try number "+ nr + " to stored chunk number "+chunkNo);
	}

	/**
	 * Function that displays the information about the number of tries that the chunk restore protocol is trying
	 * to receive a requested chunk number.
	 * @param nr - Number of tries
	 * @param chunkNo - Chunk identification number
	 */
	public static void tryNrReceiveChunk(int nr,int chunkNo){
		System.out.println("Try number "+ nr + " to receive chunk number "+chunkNo);
	}

	/**
	 * Function that displays the information about the achievement of backing up a chunk file
	 * with a certain replication degree.
	 * @param chunkNo - Chunk identification number
	 */
	public static void allChunksNrStored(int chunkNo){
		System.out.println("All Chunks with number "+ chunkNo+ " Stored");
	}

	/**
	 * Function that displays the information about the failure in achieving the number of
	 * replication degree for a certain chunk number.
	 * @param chunkNo - Chunk identification number
	 * @param stored - Atual replication degree
	 */
	public static void chunkRepDegNotAccepted(int chunkNo, int stored){
		System.out.println(stored + " Replication Degree not pleased for chunk number " + chunkNo);
	}

	/**
	 * Function that displays the information about the incompatibility of the peers protocols.
	 */
	public static void incompatibleProcols(){
		System.out.println("The peers protocols are not compatible");
	}

	/**
	 * Function that displays the information about the failure in restoring a certain
	 * chunk number.
	 * @param chunkNo - Chunk identification number
	 */
	public static void chunkNotRestored(int chunkNo){
		System.out.println("Chunk number " + chunkNo + " of file not restored");
	}

	/**
	 * Function that displays the information about the success in receiving a previously
	 * requested chunk number.
	 * @param chunkNo - Chunk identification number
	 */
	public static void chunkRestores(int chunkNo){
		System.out.println("Chunk number " + chunkNo + " restored");
	}

	/*
	 * Exceptions
	 */

	/**
	 * Function that displays the information about an exception error in the function of
	 * a certain class.
	 * @param function - Function where the exception occurred
	 * @param myClass - Class where the exception occurred
	 * @param error
	 */
	public static void exception(String function, String myClass, String error){
		System.out.println("Exception at "+function+ " from class "+myClass+" : "+error);
	}

	/*
	 * Client
	 */

	/**
	 * Function that displays the information about the server response.
	 * @param response - Server response
	 */
	public static void serverResponse(String response){
		System.out.println("Server response : "+response);
	}

	/**
	 * Function that displays further details on how to exit.
	 */
	public static void enter(){
		System.out.println("Press ENTER to exit...");
	}

	/*
	 * Server
	 */

	/**
	 * Function that display the state of the server for communications
	 */
	public static void rmiReady() {
		System.out.println("Server ready!");
	}

	/**
	 * Function that displays the message 'updated' after updating services.
	 * @param string
	 */
	public static void updated(String string) {
		System.out.println("Chunks "+string+" updated");
	}

	/**
	 * Function that displays the local where the serialized object was loaded or saved.
	 * @param string
	 */
	public static void serializeWarn(String string, int ID) {
		System.out.println("Serialized data "+string+" peersDisk/peer"+ID+"/record.ser");
	}

	/**
	 * Function that displays the information of server state
	 * @param string
	 */
	public static void checkChunks(String string) {
		System.out.println("Checking chunks "+string+"...");
	}


}
