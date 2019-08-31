package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class RemoveClient implements Runnable{
	HashMap<Integer, Socket> idSocketMap;
	int id;
	
	public RemoveClient(HashMap<Integer, Socket> idSocketMap,int id) {
		super();
		this.idSocketMap = idSocketMap;
		this.id = id;
	}

	@Override
	public void run() {
		OutputStream socketOut;
		
		Collection<Socket> socketList  = idSocketMap.values();
		Iterator<Socket> it = socketList.iterator();
		
		while(it.hasNext())
		{
			Socket tempScoket = it.next();

			try {
				socketOut = tempScoket.getOutputStream();
				
				PrintWriter writer = new PrintWriter(socketOut);
				writer.println(Constant.RID+id);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
