package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class ReceiveFile implements Runnable {

	// 由服务套接字产生的 套接字
	Socket tempSocket;
	// 用于读取
	InputStream inSocket;
	// 随机访问文件
	File file = null;
	// 临时缓冲区
	byte byteBuffer[] = new byte[1024];

	Client client;

	boolean isCompress;// 是否压缩

	boolean isEncipherment;// 是否加密

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
			client.showMassage("您正在接收了来自客户端 "+(ufid>>8)+" 的文件"+file.getName()+"...");
			RandomAccessFile inFile  = new RandomAccessFile(file, "rw");
			
			// 获取输入流
			this.inSocket = tempSocket.getInputStream();

			// 以下为传送文件代码和 套接字清理工作
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
			// 关闭流
			inSocket.close();
			client.getClientFream().hintMessage("已接收成功", "提示!");
			client.showMassage("您已成功接收了来自客户端 "+(ufid>>8)+" 的文件"+file.getName()+"。");
			// 关闭文件
			inFile.close();
			inSocket.close();
			amount = -1;

			// 关闭临时套接字
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
