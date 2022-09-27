package broadcastsemaphore;

public class Message {

	int senderID;
	MessageType type;
	int timeS;	

	enum MessageType{
		reqP,reqV,VOP,POP,ACK
	}

	

	public Message(int senderID, MessageType type, int timeS) {
		super();
		this.senderID = senderID;
		this.type = type;
		this.timeS = timeS;
	
	}

	public int getSenderID() {
		return senderID;
	}


	public void setSenderID(int senderID) {
		this.senderID = senderID;
	}


	public MessageType getType() {
		return type;
	}


	public void setType(MessageType type) {
		this.type = type;
	}


	public int getTimeS() {
		return timeS;
	}


	public void setTimeS(int timeS) {
		this.timeS = timeS;
	}
	
	
}
