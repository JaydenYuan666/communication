package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SendFile implements Runnable {

	// ��ʱ�׽���
	Socket tempSocket;
	// �����ļ��õ������
	OutputStream outSocket;
	// �����͵��ļ�
	RandomAccessFile outFile;
	// �����ļ��õ���ʱ������
	Client client;
	byte byteBuffer[] = new byte[1024];
	String fileName;
	byte fid;
	boolean isCompress;// �Ƿ�ѹ��
	boolean isEncipherment;// �Ƿ����

	public SendFile(Client client, Socket tempSocket, RandomAccessFile outFile,
			byte fid, String fileName, boolean isCompress,
			boolean isEncipherment) {
		try {
			this.tempSocket = tempSocket;
			this.outFile = outFile;// ���ļ������������
			this.client = client;
			this.fid = fid;
			this.fileName = fileName;
			this.isCompress = isCompress;
			this.isEncipherment = isEncipherment;
		} catch (Exception e) {
		}
	}

	public void run() {
		try {

			outSocket = tempSocket.getOutputStream();
			int amount;
			System.out.println("��ʼ�����ļ�...");
			

			while ((amount = outFile.read(byteBuffer)) != -1) {
				byte[] byteSendBuffer = byteBuffer;
				if(isCompress)
				{
					byteSendBuffer = GZipUtils.compress(byteSendBuffer);
					amount = byteSendBuffer.length;
				}
//				if(isEncipherment)
//				{
//					byteSendBuffer = EncryptUtil.encrypt(new String(byteSendBuffer).getBytes());
//					amount = byteSendBuffer.length;
//				}

				outSocket.write(byteSendBuffer, 0, amount);
			}
			javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(),
					"�ѷ������", "��ʾ!", javax.swing.JOptionPane.PLAIN_MESSAGE);

			outFile.close();
			outSocket.close();
			tempSocket.close();
		} catch (SocketTimeoutException e) {
			
		}catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		
		client.removeIdToFile(fid);
		client.showMassage(fileName + "�ѷ������");
	}
}
