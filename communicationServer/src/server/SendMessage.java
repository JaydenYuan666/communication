package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SendMessage  implements Runnable {

	// ���͵���Ϣ
	private String massage;
	// ��ʱ�׽���
	private Socket tempSocket;

	public SendMessage(String massage, Socket tempSocket) {
		this.massage = massage;
		this.tempSocket = tempSocket;
	}

	/**
	 * �Ⱦ������Ƿ��������ȿ���
	 * 
	 */
	public void run() {
		// ͷ��Э��[������id,�������ǳ�,��Ϣ����(��Ӧ��ȷ�Ϻʹ��䣩,����,ͼƬ,�ļ�,�������ļ�),/*�м���0b11111111�ָ�*/
		// ��������ļ����ļ�id,�ļ�����,�ļ���С]/*�м���0b11111111�ָ�*/
		// ���������Ϣ����Ϣ����
		//
		// ����һ��ȷ��֡��[������id,��Ӧ��Ϣ,(��Ϣ����)�Ƿ����,ȷ���ļ�id]
		// ����Э��,������id,�ļ�id,��Ϣ����(����),
		OutputStream socketOut;
		try {
			socketOut = tempSocket.getOutputStream();
			PrintWriter writer = new PrintWriter(socketOut);
			writer.println(massage);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
