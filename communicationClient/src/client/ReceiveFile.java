package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class ReceiveFile implements Runnable {

	// �ɷ����׽��ֲ����� �׽���
	Socket tempSocket;
	// ���ڶ�ȡ
	InputStream inSocket;
	// ��������ļ�
	File file = null;
	// ��ʱ������
	byte byteBuffer[] = new byte[1024];

	Client client;

	boolean isCompress;// �Ƿ�ѹ��

	boolean isEncipherment;// �Ƿ����

	int ufid;


	public ReceiveFile(Client client, int id, Socket tempSocket,
			File file) {
		this.tempSocket = tempSocket;

		this.file = file;
		

		this.client = client;
		this.ufid = id;
	}

	public void run() {
		try {
//			File tempFile= file;
			client.showMassage("�����ڽ��������Կͻ��� "+(ufid>>8)+" ���ļ�"+file.getName()+"...");
			RandomAccessFile inFile  = new RandomAccessFile(file, "rw");
			
			// ��ȡ������
			this.inSocket = tempSocket.getInputStream();

			// ����Ϊ�����ļ������ �׽���������
			int amount;

			while ((amount = inSocket.read(byteBuffer)) != -1) {
				
				byte[] byteSendBuffer = byteBuffer;
//				if(isEncipherment)
//				{
//					byteSendBuffer = new String(EncryptUtil.decrypt(byteSendBuffer)).getBytes();
//					amount = byteSendBuffer.length;
//				}

				inFile.write(byteSendBuffer, 0, amount);
			}
			// �ر���
			inSocket.close();
			client.getClientFream().hintMessage("�ѽ��ճɹ�", "��ʾ!");
			client.showMassage("���ѳɹ����������Կͻ��� "+(ufid>>8)+" ���ļ�"+file.getName()+"��");
			// �ر��ļ�
			inFile.close();
			inSocket.close();
			amount = -1;

			// �ر���ʱ�׽���
			tempSocket.close();
			client.removeReceiveFileItem(ufid);

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
