package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SendFile implements Runnable {

	// 临时套接字
	Socket tempSocket;
	// 发送文件用的输出流
	OutputStream outSocket;
	// 欲发送的文件
	RandomAccessFile outFile;
	// 发送文件用的临时缓存区
	Client client;
	byte byteBuffer[] = new byte[1024];
	String fileName;
	byte fid;
	boolean isCompress;// 是否压缩
	boolean isEncipherment;// 是否加密

	public SendFile(Client client, Socket tempSocket, RandomAccessFile outFile,
			byte fid, String fileName, boolean isCompress,
			boolean isEncipherment) {
		try {
			this.tempSocket = tempSocket;
			this.outFile = outFile;// 对文件进行随机操作
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
			System.out.println("开始发送文件...");
			

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
					"已发送完毕", "提示!", javax.swing.JOptionPane.PLAIN_MESSAGE);

			outFile.close();
			outSocket.close();
			tempSocket.close();
		} catch (SocketTimeoutException e) {
			
		}catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		
		client.removeIdToFile(fid);
		client.showMassage(fileName + "已发送完毕");
	}
}
