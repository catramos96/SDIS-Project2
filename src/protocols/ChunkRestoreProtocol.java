package protocols;

import resources.Util;
import message.ProtocolMessage;
import network.GroupChannel;

/**
 * Class ChunkRestoreProtocol used to restore a chunk of a file.
 */
public class ChunkRestoreProtocol extends Thread {
    private final GroupChannel channel;
    private final ProtocolMessage msg;
    
	private static final int MAX_TRIES = 5;

    /**
     * Constructor of ChunkRestoreProtocol
     */
    public ChunkRestoreProtocol(GroupChannel channel, ProtocolMessage msg) {
        this.channel = channel;
        this.msg = msg;
    }

    @Override
    public void run() {
        channel.sendMessageToRoot(msg);
        System.out.println("Sent getChunk");
    }
}
