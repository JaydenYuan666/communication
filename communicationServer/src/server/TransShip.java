package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TransShip implements Runnable {
	// �ɷ����׽��ֲ����� �׽���
	Socket senderSocket;
	Socket receieceSocket;
	// ��������ļ�
	// ��ʱ������
	byte byteBuffer[] = new byte[1024];

	Server server;

	int ufid;

	public TransShip(Socket senderSocket, Socket receieceSocket, Server server,
			int ufid) {
		super();
		this.senderSocket = senderSocket;
		this.receieceSocket = receieceSocket;
		this.server = server;
		this.ufid = ufid;
	}

	@Override
	public void run() {

		// server.showMassage("��������ת�����Կͻ��� "+(ufid>>8)+" ���ļ�"+fileName+"...");

		try {
			// ��ȡ������
			InputStream in = senderSocket.getInputStream();
			OutputStream out = receieceSocket.getOutputStream();
			// ����Ϊ�����ļ������ �׽���������
			int amount;

			while ((amount = in.read(byteBuffer)) != -1) {

				byte[] byteSendBuffer = byteBuffer;

				out.write(byteSendBuffer, 0, amount);
			}
			// �ر���
			in.close();
			out.close();
			// server.showMassage("���ѳɹ����������Կͻ��� "+(ufid>>8)+" ���ļ�"+file.getName()+"��");
			// �ر���ʱ�׽���
			senderSocket.close();
			receieceSocket.close();
			server.removeSRItem(ufid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
