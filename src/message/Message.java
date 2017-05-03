package message;

public abstract class Message {
	protected Enum<?> type;
	
	public abstract byte[] buildMessage();
}
