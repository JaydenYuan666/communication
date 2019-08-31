package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SendClintID implements Runnable{
	Socket allsendID;
	HashMap<Integer, Socket> idSocketMap;
	int id;
	
	public SendClintID(HashMap<Integer, Socket> idSocketMap,int id,Socket allsendID) {
		super();
		this.idSocketMap = idSocketMap;
		this.id = id;
		this.allsendID = allsendID;
	}

	@Override
	public void run() {
		OutputStream socketOut;
		Set<Integer> keys = idSocketMap.keySet();
		Iterator<Integer> itkeys = keys.iterator();
		String allkey = "";
		while(itkeys.hasNext())
		{
			allkey += itkeys.next()+Constant.DIVIDE;
		}
		
		
		try {
			socketOut = allsendID.getOutputStream();
			PrintWriter writer = new PrintWriter(socketOut);
			writer.println(Constant.IDS+allkey);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collection<Socket> socketList  = idSocketMap.values();
		Iterator<Socket> it = socketList.iterator();
		
		while(it.hasNext())
		{
			Socket tempScoket = it.next();
			if(allsendID==tempScoket)
			{
				continue;
			}
			try {
				socketOut = tempScoket.getOutputStream();
				
				PrintWriter writer = new PrintWriter(socketOut);
				writer.println(Constant.ID+id);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
