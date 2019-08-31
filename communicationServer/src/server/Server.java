package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

public class Server {
	private ServerSocket socket;
	private int port = 10001;
	private ExecutorService threadPool;// 线程池
	// private BlockingQueue<String> fileQueue;// 双缓冲队列
	HashMap<Integer, Socket> idSocketMap;
	private int allocationId;// 分配给客户端的id
	private final int id = 0;// 服务器的id设置为0
	private ServerFrame serverFream;
	HashMap<Integer, File> idFileMap;
	HashMap<Integer, Receiever> idReceiveFileMap;
	HashMap<Integer, Sender> idSenderFileMap;
	HashMap<Integer, Socket> unConnectidSocket;

	public Server(ServerFrame serverFream) {
		
		try {
			while(NetUtil.isLoclePortUsing(port)) {
				port = port+1;
			}
			socket = new ServerSocket(port);
			allocationId = 1;
			idSocketMap = new HashMap<Integer, Socket>();
			idFileMap = new HashMap<Integer, File>();
			idReceiveFileMap = new HashMap<Integer, Receiever>();
			idSenderFileMap = new HashMap<Integer, Sender>();
			unConnectidSocket = new HashMap<Integer, Socket>();
			threadPool = Executors.newFixedThreadPool(60);// 固定大小的线程池
			// fileQueue = new LinkedBlockingQueue<String>(1000);// 固定大小的双缓冲队列
			this.serverFream = serverFream;
		} catch (IOException e) {
			e.printStackTrace();
		
		}
	}

	public void closeConnect() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(JTextArea showMessage) {
		String message;
		while (true) {
			System.out.println("等待客户端" + allocationId + "的连接。。。");

			try {
				// 将线程体交给线程池，线程池会自动分配一个空线程执行
				Socket client = socket.accept();
				byte[] buffer = new byte[1];
				InputStream socketIn = client.getInputStream();
				PrintWriter writer = new PrintWriter(client.getOutputStream());
				writer.println(getAllocationId());
				writer.flush();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(socketIn));
				int amount = socketIn.read();
				if (amount == -1) {
					continue;
				}
				switch (amount) {
				case Constant.CONNECT:
					idSocketMap.put(allocationId, client);
					serverFream.addComItem(allocationId);
					threadPool.execute(new ReceiveMessage(this, client,
							showMessage, allocationId));
					threadPool.execute(new SendClintID(idSocketMap,
							allocationId, client));
					break;
				case Constant.RECONNECT:
					int clientid = Integer.parseInt(reader.readLine());
					if (unConnectidSocket.get(clientid) == null) {
						sendMassage(0, "请退出客户端重新连接", client);
						return;
					}
					idSocketMap.put(clientid, client);// 覆盖掉以前的clientid
					unConnectidSocket.remove(clientid);
					threadPool.execute(new ReceiveMessage(this, client,
							showMessage, clientid));
					threadPool.execute(new SendClintID(idSocketMap, clientid,
							client));
					serverFream.addComItem(clientid);
					break;
				case Constant.RECEIVEFILE:
					System.out.println("ser接收文件");
					message = reader.readLine();
					String[] colAcks = message.split(Constant.DIVIDE);
					int receieveID = Integer.parseInt(colAcks[0]);
					int fid = Integer.parseInt(colAcks[1]);
					Sender sender = idSenderFileMap.get(fid);
					if (sender == null) {
						idReceiveFileMap.put(fid, new Receiever(receieveID,
								client));
						continue;
					}
					if (sender.match(receieveID)) {
						Socket senderSocket = sender.getrSocket();
						threadPool.execute(new TransShip(senderSocket, client,
								this, fid));
					} else {
						idReceiveFileMap.put(fid, new Receiever(receieveID,
								client));
					}
					break;
				case Constant.SENDF:
					System.out.println("ser发送文件");
					message = reader.readLine();
					String[] colacks = message.split(Constant.DIVIDE);
					int receieveId = Integer.parseInt(colacks[0]);
					int ufid = Integer.parseInt(colacks[1]);
					if (receieveId == id) {
						File file = idFileMap.get(ufid);
						// System.out.println(file);
						if (file != null) {
							threadPool.execute(new ReceiveFile(this, id,
									client, file));

						}
						System.out.println("接收文件");
						continue;
					}

					Receiever receiever = idReceiveFileMap.get(ufid);
					if (receiever == null) {
						idSenderFileMap.put(ufid,
								new Sender(receieveId, client));
						continue;
					}
					if (receiever.match(receieveId)) {
						Socket receieverSocket = receiever.getrSocket();
						threadPool.execute(new TransShip(client,
								receieverSocket, this, ufid));
					} else {
						idReceiveFileMap.put(ufid, new Receiever(receieveId,
								client));
					}
					break;
				default:
					System.out.println("default:" + buffer[0]);
				}
			} catch (SocketException e) {

				System.exit(0);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public int getAllocationId() {
		for (int i = allocationId; i < Integer.MAX_VALUE; i++) {
			if (idSocketMap.get(i) == null) {
				allocationId = i;
				break;
			}
		}
		return allocationId;
	}

	public void removeSRItem(int fid) {
		idSenderFileMap.remove(fid);
		idReceiveFileMap.remove(fid);
	}

	// 发送消息
	public void sendMassage(int senderID, String massage, Socket socket) {

		threadPool.execute(new SendMessage(Constant.SENDMESSAGE + senderID
				+ Constant.DIVIDE + massage, socket));
	}

	public void sendMassage(String massage, Socket socket) {

		threadPool.execute(new SendMessage(massage, socket));
	}

	//
	public void showMassage(String massage) {
		serverFream.showMassageToTextArea(massage, "");

	}

	public void showMassage(String massage, String who) {
		serverFream.showMassageToTextArea(massage, who);

	}

	public String getIPAndProt() {
		String IP = "";
		try {
			IP = InetAddress.getLocalHost() + "";
			IP = IP.split("/")[1];
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return IP + " : " + port;
	}

	public void close() {
		closeConnect();
	}

	public HashMap<Integer, Socket> getIdSocketMap() {
		return idSocketMap;
	}

	public void setIdSocketMap(HashMap<Integer, Socket> idSocketMap) {
		this.idSocketMap = idSocketMap;
	}

	public void removeIdToFileItem(int id) {
		idFileMap.remove(id);
	}

	public void removeIdToSocketItem(int clientId) {
		unConnectidSocket.put(clientId, idSocketMap.get(clientId));
		idSocketMap.remove(clientId);
		threadPool.execute(new RemoveClient(idSocketMap, clientId));
	}

	public int getId() {
		return id;
	}

	public Set<Integer> getIDKey() {

		return idSocketMap.keySet();
	}

	public HashMap<Integer, File> getIdFileMap() {
		return idFileMap;
	}

	public void addItemToidFileMap(int id, File inFile) {
		idFileMap.put(id, inFile);
	}

	public void sendMassage(int senderID, String msg, int clientId) {
		Socket socket = idSocketMap.get(clientId);
		threadPool.execute(new SendMessage(Constant.SENDMESSAGE + senderID
				+ Constant.DIVIDE + msg, socket));
	}

	public void sendENMassage(int senderID, String msg, int clientId) {
		Socket socket = idSocketMap.get(clientId);
		threadPool.execute(new SendMessage(Constant.EnMESSAGE + senderID
				+ Constant.DIVIDE + msg, socket));
	}

	public void sendMassage(String msg, int clientId) {
		Socket socket = idSocketMap.get(clientId);
		threadPool.execute(new SendMessage(msg, socket));
	}

	public void deleteItem(int clientId) {
		serverFream.deleteItem(clientId);

	}

	public ServerFrame getServerFream() {
		return serverFream;
	}

	public void setServerFream(ServerFrame serverFream) {
		this.serverFream = serverFream;
	}

}
