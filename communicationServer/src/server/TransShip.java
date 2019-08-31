package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TransShip implements Runnable {
	// 由服务套接字产生的 套接字
	Socket senderSocket;
	Socket receieceSocket;
	// 随机访问文件
	// 临时缓冲区
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

		// server.showMassage("您正在中转了来自客户端 "+(ufid>>8)+" 的文件"+fileName+"...");

		try {
			// 获取输入流
			InputStream in = senderSocket.getInputStream();
			OutputStream out = receieceSocket.getOutputStream();
			// 以下为传送文件代码和 套接字清理工作
			int amount;

			while ((amount = in.read(byteBuffer)) != -1) {

				byte[] byteSendBuffer = byteBuffer;

				out.write(byteSendBuffer, 0, amount);
			}
			// 关闭流
			in.close();
			out.close();
			// server.showMassage("您已成功接收了来自客户端 "+(ufid>>8)+" 的文件"+file.getName()+"。");
			// 关闭临时套接字
			senderSocket.close();
			receieceSocket.close();
			server.removeSRItem(ufid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
