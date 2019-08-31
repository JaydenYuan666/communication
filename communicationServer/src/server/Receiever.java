package server;

import java.net.Socket;

public class Receiever {

	private int receiveID;
	private Socket rSocket;

	public Receiever(int receiveID,  Socket rSocket) {
		super();
		this.receiveID = receiveID;
		this.setrSocket(rSocket);
	}

	public boolean match(int receiveID) {
		if(this.receiveID==receiveID)
			return true;
		return false;
	}

	public Socket getrSocket() {
		return rSocket;
	}

	public void setrSocket(Socket rSocket) {
		this.rSocket = rSocket;
	}
}
