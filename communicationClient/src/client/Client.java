package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Client {

	private Socket socket;
	private int prot;

	private ExecutorService threadPool;// 线程池
	private int clientId;
	private byte fileId;
	private HashMap<Byte, String> filedToPathMap;// 发送的文件
	private HashMap<Integer, String> receiveFiledToPathMap;// 发送的文件
	private String ip;
	private CilentFrame clientFream;
	private Timer heartTimer;
	private boolean close = true; // 关闭连接标志位，true表示关闭，false表示连接
	private Integer tasktime = 1 * 1 * 1000;// 心跳包发送间隔，以毫秒为单位 1秒
	private Integer sotimeout = 1 * 1 * 3000;// 超时时间，以毫秒为单位 3秒
	private HeartTask heartTask;
	public long lastDate = new Date().getTime();

	public Client(CilentFrame clientFream) {
		threadPool = Executors.newFixedThreadPool(50);// 同时最多可启动100个子线程
		fileId = 1;
		this.clientFream = clientFream;
		heartTimer = new Timer();
	}

	// 与服务器建立连接
	public boolean conectServer(String inputIP, int inputPORT) {
		ip = inputIP;
		prot = inputPORT;
		return conectServer();
	}

	public void reconectServer() {
		new Reconnect().start();
	}

	public boolean conectServer() {

		try {
			socket = new Socket(ip, prot);
			filedToPathMap = new HashMap<Byte, String>();
			receiveFiledToPathMap = new HashMap<Integer, String>();
			InputStreamReader inr = new InputStreamReader(
					socket.getInputStream());
			String massage = new BufferedReader(inr).readLine();
			socket.setKeepAlive(true);// 开启保持活动状态的套接字
			close = false;

			if (massage != null) {
				try {
					clientId = Integer.parseInt(massage);
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					System.exit(0);

				}

				PrintWriter writer = new PrintWriter(socket.getOutputStream());
				// writer.println(id+Constant.DIVIDE+"服务器："+Constant.DIVIDE+"r"+Constant.DIVIDE+"c"+Constant.DIVIDE+allocationId);
				writer.println(Constant.CONNECT);
				writer.flush();
				heartTask = new HeartTask(socket.getOutputStream());
				heartTimer.schedule(heartTask, 1000, tasktime);
			}

		} catch (UnknownHostException e) {
			System.out.println("连接失败：" + e.getMessage());
			close = true;
			return false;
		} catch (IOException e) {
			System.out.println("连接失败：" + e.getMessage());
			close = true;
			return false;
		}

		return true;
	}

	public void pushFileToPathMap(byte fid, String path) {
		filedToPathMap.put(fid, path);
	}

	// 发送文件
	public void sendFile(File file, byte fid, int receieverID) {
		try {

			Socket tempSocket = new Socket(ip, prot);
			InputStreamReader inr = new InputStreamReader(
					tempSocket.getInputStream());
			new BufferedReader(inr).readLine();
			PrintWriter writer = new PrintWriter(tempSocket.getOutputStream());
			// writer.println(id+Constant.DIVIDE+"服务器："+Constant.DIVIDE+"r"+Constant.DIVIDE+"c"+Constant.DIVIDE+allocationId);
			writer.println(Constant.SENDFILE + receieverID + Constant.DIVIDE
					+ ((clientId << 8) ^ fid));
			writer.flush();

			showMassage("正在发送文件" + file.getName() + "...");
			threadPool.execute(new SendFile(this, tempSocket,
					new RandomAccessFile(file, "r"), fid, file.getName(),
					clientFream.isCompress(), clientFream.isEncipherment()));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isServerClose() {
		try {
			if(Math.abs(lastDate-new Date().getTime())>sotimeout) {
				close = true;
				return true;
			}
			return false;
		} catch (Exception se) {
			close = true;
			return true;
		}
	}

	public boolean isClose() {
		return close;
	}

	// 发送消息
	public void sendMassage(String massage, String targetId) {
		if (isServerClose()) {
			showMassage("服务器已断开连接...");
			return;
		}

		String head = Constant.MESSAGE;
		if (clientFream.isEncipherment()) {
			massage = new String(EncryptUtil.encrypt(massage.getBytes()));
			head = Constant.EnciphermentMESSAGE;

		}

		threadPool.execute(new SendMessage(head + targetId + Constant.DIVIDE
				+ massage, socket));
	}

	public void sendMassage(String massage) {
		if (isServerClose()) {
			showMassage("服务器已断开连接!!!");
			return;
		}

		threadPool.execute(new SendMessage(massage, socket));
	}

	//
	public void showMassage(String massage) {
		clientFream.showMassage(massage, "提示");

	}

	// 发送消息
	public void sendFileHeadMsg(String path, String targetId) {

		if (isServerClose()) {
			showMassage("服务器已断开连接!!!");
			return;
		}

		File file = new File(path);
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		// Constant.CONTROL(代表请求)+"请求类型"+"文件ID"+"文件名"+"文件类型"+"文件大小"+"是否压缩"+"是否加密"
		Byte fid = getFileId();
		if (fid == null) {
			JOptionPane.showMessageDialog(new JFrame(), "发送文件已达到最大数量！！！");
			return;
		}
		if (clientFream.isCompress()) {
			fileType = ".gz";
		}
		String ack = Constant.CONTROL + targetId + Constant.DIVIDE
				+ Constant.SENDFILE + Constant.DIVIDE + fid + Constant.DIVIDE
				+ fileName + Constant.DIVIDE + fileType + Constant.DIVIDE
				+ file.length();
		pushFileToPathMap(fid, path);
		threadPool.execute(new SendMessage(ack, socket));
	}

	// 删除hashmap中文件id映射
	public void removeIdToFile(byte fid) {
		if (filedToPathMap.get(fid) != null) {

			filedToPathMap.remove(fid);
		}
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	// 开启发送文字消息线程
	public void startReceiveMessage() {
		threadPool.execute(new ReceiveMessage(this, socket, clientFream
				.getShowMessage()));
	}

	public void close() {
		try {
			heartTimer.purge();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Byte getFileId() {
		int i = fileId;
		for (; filedToPathMap.get(fileId) != null && i < 255; i++, fileId++)
			;
		if ((i ^ 255) == 0) {
			return null;
		}
		return fileId;
	}

	public void setFileId(byte fileId) {
		this.fileId = fileId;
	}

	public String getPath(byte fid) {
		return filedToPathMap.get(fid);
	}

	public void setFiledToPathMap(HashMap<Byte, String> filedToPathMap) {
		this.filedToPathMap = filedToPathMap;
	}

	public void initItem(String allid) {
		clientFream.initItem(allid);
	}

	public void addItem(int id) {
		clientFream.addItem(id);
	}

	public CilentFrame getClientFream() {
		return clientFream;
	}

	public void setClientFream(CilentFrame clientFream) {
		this.clientFream = clientFream;
	}

	public void receiveFile(File file, int source_fid) {
		try {

			Socket tempSocket = new Socket(ip, prot);
			InputStreamReader inr = new InputStreamReader(
					tempSocket.getInputStream());
			PrintWriter writer = new PrintWriter(tempSocket.getOutputStream());
			new BufferedReader(inr).readLine();
			
			writer.println(Constant.RECEIVEFILE + clientId + Constant.DIVIDE
					+ source_fid);
			writer.flush();


			threadPool.execute(new ReceiveFile(this, source_fid, tempSocket,
					file));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pushReceiveFileToPathMap(int source_fid, String path) {
		// 此处有点问题，应该把发送文件人的信息保存下来
		receiveFiledToPathMap.put(source_fid, path);
	}

	public void removeReceiveFileItem(int ufid) {
		receiveFiledToPathMap.remove(ufid);

	}

	class HeartTask extends TimerTask {
		private OutputStream out;

		public void setOut(OutputStream out) {
			this.out = out;
		}

		public HeartTask(OutputStream out) {
			this.out = out;
		}

		@Override
		public void run() {
			PrintWriter write = new PrintWriter(out);
			synchronized (out) {
				write.print(Constant.HEART);

				write.flush();
			}
		}
	}

	class Reconnect extends Thread {
		public boolean connect = true;

		@Override
		public void run() {
			while (connect) {
				try {
					socket = new Socket(ip, prot);
					PrintWriter writer = new PrintWriter(
							socket.getOutputStream());
					writer.println(Constant.RECONNECT + clientId);
					writer.flush();
					startReceiveMessage();
					clientFream.showMassage("重新连接成功。。。", "提示");
					connect = false;
					heartTask.setOut(socket.getOutputStream());
				} catch (UnknownHostException e) {
					clientFream.showMassage("重新连接失败，正在尝试下一次重新连接...", "提示");

				} catch (IOException e) {
					clientFream.showMassage("重新连接失败，正在尝试下一次重新连接...", "提示");
				}
				try {
					sleep(sotimeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void removeItem(int parseInt) {
		clientFream.removeItem(parseInt);
		
	}
}
