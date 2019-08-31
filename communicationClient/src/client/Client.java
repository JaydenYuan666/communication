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

	private ExecutorService threadPool;// �̳߳�
	private int clientId;
	private byte fileId;
	private HashMap<Byte, String> filedToPathMap;// ���͵��ļ�
	private HashMap<Integer, String> receiveFiledToPathMap;// ���͵��ļ�
	private String ip;
	private CilentFrame clientFream;
	private Timer heartTimer;
	private boolean close = true; // �ر����ӱ�־λ��true��ʾ�رգ�false��ʾ����
	private Integer tasktime = 1 * 1 * 1000;// ���������ͼ�����Ժ���Ϊ��λ 1��
	private Integer sotimeout = 1 * 1 * 3000;// ��ʱʱ�䣬�Ժ���Ϊ��λ 3��
	private HeartTask heartTask;
	public long lastDate = new Date().getTime();

	public Client(CilentFrame clientFream) {
		threadPool = Executors.newFixedThreadPool(50);// ͬʱ��������100�����߳�
		fileId = 1;
		this.clientFream = clientFream;
		heartTimer = new Timer();
	}

	// ���������������
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
			socket.setKeepAlive(true);// �������ֻ״̬���׽���
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
				// writer.println(id+Constant.DIVIDE+"��������"+Constant.DIVIDE+"r"+Constant.DIVIDE+"c"+Constant.DIVIDE+allocationId);
				writer.println(Constant.CONNECT);
				writer.flush();
				heartTask = new HeartTask(socket.getOutputStream());
				heartTimer.schedule(heartTask, 1000, tasktime);
			}

		} catch (UnknownHostException e) {
			System.out.println("����ʧ�ܣ�" + e.getMessage());
			close = true;
			return false;
		} catch (IOException e) {
			System.out.println("����ʧ�ܣ�" + e.getMessage());
			close = true;
			return false;
		}

		return true;
	}

	public void pushFileToPathMap(byte fid, String path) {
		filedToPathMap.put(fid, path);
	}

	// �����ļ�
	public void sendFile(File file, byte fid, int receieverID) {
		try {

			Socket tempSocket = new Socket(ip, prot);
			InputStreamReader inr = new InputStreamReader(
					tempSocket.getInputStream());
			new BufferedReader(inr).readLine();
			PrintWriter writer = new PrintWriter(tempSocket.getOutputStream());
			// writer.println(id+Constant.DIVIDE+"��������"+Constant.DIVIDE+"r"+Constant.DIVIDE+"c"+Constant.DIVIDE+allocationId);
			writer.println(Constant.SENDFILE + receieverID + Constant.DIVIDE
					+ ((clientId << 8) ^ fid));
			writer.flush();

			showMassage("���ڷ����ļ�" + file.getName() + "...");
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

	// ������Ϣ
	public void sendMassage(String massage, String targetId) {
		if (isServerClose()) {
			showMassage("�������ѶϿ�����...");
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
			showMassage("�������ѶϿ�����!!!");
			return;
		}

		threadPool.execute(new SendMessage(massage, socket));
	}

	//
	public void showMassage(String massage) {
		clientFream.showMassage(massage, "��ʾ");

	}

	// ������Ϣ
	public void sendFileHeadMsg(String path, String targetId) {

		if (isServerClose()) {
			showMassage("�������ѶϿ�����!!!");
			return;
		}

		File file = new File(path);
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		// Constant.CONTROL(��������)+"��������"+"�ļ�ID"+"�ļ���"+"�ļ�����"+"�ļ���С"+"�Ƿ�ѹ��"+"�Ƿ����"
		Byte fid = getFileId();
		if (fid == null) {
			JOptionPane.showMessageDialog(new JFrame(), "�����ļ��Ѵﵽ�������������");
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

	// ɾ��hashmap���ļ�idӳ��
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

	// ��������������Ϣ�߳�
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
		// �˴��е����⣬Ӧ�ðѷ����ļ��˵���Ϣ��������
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
					clientFream.showMassage("�������ӳɹ�������", "��ʾ");
					connect = false;
					heartTask.setOut(socket.getOutputStream());
				} catch (UnknownHostException e) {
					clientFream.showMassage("��������ʧ�ܣ����ڳ�����һ����������...", "��ʾ");

				} catch (IOException e) {
					clientFream.showMassage("��������ʧ�ܣ����ڳ�����һ����������...", "��ʾ");
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
