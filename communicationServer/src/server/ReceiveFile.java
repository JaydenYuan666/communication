package server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;

public class ReceiveFile implements Runnable {

	// �ɷ����׽��ֲ����� �׽���
	Socket tempSocket;
	// ���ڶ�ȡ
	InputStream inSocket;
	// ��������ļ�
	File file = null;
	// ��ʱ������
	byte byteBuffer[] = new byte[1024];

	Server server;

	boolean isCompress;// �Ƿ�ѹ��

	boolean isEncipherment;// �Ƿ����

	int ufid;

	public ReceiveFile(Server server, int id, Socket tempSocket, File file,
			boolean isCompress, boolean isEncipherment) {
		this.tempSocket = tempSocket;

		this.file = file;

		this.server = server;
		this.ufid = id;
		this.isCompress = isCompress;
		this.isEncipherment = isEncipherment;
	}

	public ReceiveFile(Server server, int id, Socket client, File file) {
		this.tempSocket = client;

		this.file = file;

		this.server = server;
		this.ufid = id;
	}

	public void run() {
		try {
			// File tempFile= file;
			server.showMassage("�����ڽ��������Կͻ��� " + (ufid >> 8) + " ���ļ�"
					+ file.getName() + "...");
			RandomAccessFile inFile = new RandomAccessFile(file, "rw");

			// ��ȡ������
			this.inSocket = tempSocket.getInputStream();

			// ����Ϊ�����ļ������ �׽���������
			int amount;

			while ((amount = inSocket.read(byteBuffer)) != -1) {

				byte[] byteSendBuffer = byteBuffer;

				inFile.write(byteSendBuffer, 0, amount);
			}
			// �ر���
			inSocket.close();
			server.getServerFream().hintMessage("�ѽ��ճɹ�", "��ʾ!");
			server.showMassage("���ѳɹ����������Կͻ��� " + (ufid >> 8) + " ���ļ�"
					+ file.getName() + "��      ("
					+ Constant.format.format(new Date()) + ")");
			// �ر��ļ�
			inFile.close();
			inSocket.close();
			amount = -1;

			// �ر���ʱ�׽���
			tempSocket.close();
			server.removeIdToFileItem(ufid);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception ex) {
			System.out.println(ex.toString());
			ex.printStackTrace();
			return;
		}
	}
}
